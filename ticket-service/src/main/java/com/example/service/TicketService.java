package com.example.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.event.TicketBookedEvent;
import com.example.exception.ResourceNotFoundException;
import com.example.feign.FlightInterface;
import com.example.feign.PassengerInterface;
import com.example.model.Ticket;
import com.example.repository.TicketRepository;
import com.example.request.BookTicketRequest;
import com.example.response.FlightResponse;
import com.example.response.PassengerDetailsResponse;
import com.example.response.SeatMapResponse;
import com.example.response.TicketResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final PassengerInterface passengerInterface;
    private final FlightInterface flightInterface;
    private final KafkaTemplate<String, TicketBookedEvent> kafkaTemplate;

    public TicketService(
            TicketRepository ticketRepository,
            PassengerInterface passengerInterface,
            FlightInterface flightInterface,
            KafkaTemplate<String, TicketBookedEvent> kafkaTemplate) {

        this.ticketRepository = ticketRepository;
        this.passengerInterface = passengerInterface;
        this.flightInterface = flightInterface;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<String> bookTicketService(BookTicketRequest req) {

        if (req.getPassengerIds() == null || req.getPassengerIds().isEmpty()) {
            throw new IllegalArgumentException("Passenger list cannot be empty");
        }

        if (req.getSeatNumbers() == null || req.getSeatNumbers().isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat");
        }

        if (req.getPassengerIds().size() != req.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Passengers and seats count must match");
        }

        List<String> normalizedSeats = normalizeSeats(req.getSeatNumbers());

        FlightResponse flight = flightInterface.getByID(req.getFlightId()).getBody();

        if (flight == null) {
            throw new ResourceNotFoundException("Flight not found");
        }

        validateSeatNumbers(normalizedSeats, flight.getTotalSeats());

        if (flight.getAvailableSeats() < normalizedSeats.size()) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        List<String> alreadyBooked = ticketRepository.findBookedSeatNumbers(req.getFlightId())
                .stream()
                .map(this::normalizeSeat)
                .toList();

        Set<String> conflicts = normalizedSeats.stream()
                .filter(alreadyBooked::contains)
                .collect(Collectors.toSet());

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Seats already booked: " + String.join(", ", conflicts));
        }

        int seats = req.getPassengerIds().size();
        flightInterface.reserveSeats(req.getFlightId(), seats);

        String pnr = UUID.randomUUID().toString();

        Ticket ticket = Ticket.builder()
                .pnr(pnr)
                .flightId(req.getFlightId())
                .seatNumbers(normalizedSeats)
                .passengerIds(req.getPassengerIds())
                .numberOfSeats(seats)
                .booked(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        sendKafkaNotifications(saved);

        return ResponseEntity.ok(saved.getPnr());
    }

    @Transactional
    public ResponseEntity<String> deleteTicketById(int ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.isBooked()) {
            return ResponseEntity.ok("Ticket already cancelled");
        }

        FlightResponse flight = flightInterface.getByID(ticket.getFlightId()).getBody();
        if (flight == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Flight service unavailable");
        }

        if (LocalDateTime.now().plusHours(24).isAfter(flight.getDepartureTime())) {
            return ResponseEntity.badRequest()
                    .body("Ticket cannot be cancelled within 24 hours of departure");
        }

        flightInterface.releaseSeats(ticket.getFlightId(), ticket.getNumberOfSeats());
        ticket.setBooked(false);
        ticketRepository.save(ticket);

        return ResponseEntity.ok("Ticket cancelled successfully");
    }

    @CircuitBreaker(name = "flightService", fallbackMethod = "getByPnrFallback")
    public ResponseEntity<TicketResponse> getByPnrService(String pnr) {

        Ticket ticket = ticketRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        FlightResponse flight = flightInterface.getByID(ticket.getFlightId()).getBody();
        if (flight == null) {
            throw new ResourceNotFoundException("Flight details unavailable");
        }

        List<PassengerDetailsResponse> passengers = fetchPassengers(ticket.getPassengerIds());

        return ResponseEntity.ok(buildResponse(ticket, flight, passengers));
    }

    public ResponseEntity<TicketResponse> getByPnrFallback(String pnr, Throwable ex) {
        logger.warn("getByPnr fallback | pnr={} | {}", pnr, ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @CircuitBreaker(name = "passengerService", fallbackMethod = "getTicketsByEmailFallback")
    public ResponseEntity<List<TicketResponse>> getTicketsByEmailService(String email) {

        Integer passengerId = passengerInterface.getIdByEmail(email).getBody();
        if (passengerId == null) {
            return ResponseEntity.notFound().build();
        }

        List<Ticket> tickets = ticketRepository.findAllByPassengerId(passengerId);

        List<TicketResponse> responses = tickets.stream().map(ticket -> {

            FlightResponse flight =
                    flightInterface.getByID(ticket.getFlightId()).getBody();

            if (flight == null) {
                throw new ResourceNotFoundException("Flight details unavailable");
            }

            List<PassengerDetailsResponse> passengers =
                    fetchPassengers(ticket.getPassengerIds());

            return buildResponse(ticket, flight, passengers);

        }).toList();

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<TicketResponse>> getTicketsByEmailFallback(
            String email, Throwable ex) {

        logger.warn("getTicketsByEmail fallback | email={} | {}", email, ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    private List<PassengerDetailsResponse> fetchPassengers(List<Integer> passengerIds) {

        List<PassengerDetailsResponse> passengers = new ArrayList<>();

        for (Integer pid : passengerIds) {
            PassengerDetailsResponse passenger =
                    passengerInterface.getPassengerDetails(pid).getBody();

            if (passenger == null) {
                throw new ResourceNotFoundException("Passenger not found: " + pid);
            }
            passengers.add(passenger);
        }
        return passengers;
    }

    private TicketResponse buildResponse(
            Ticket ticket,
            FlightResponse flight,
            List<PassengerDetailsResponse> passengers) {

        return TicketResponse.builder()
                .id(ticket.getTicketId())
                .pnr(ticket.getPnr())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .seatNumbers(ticket.getSeatNumbers())
                .numberOfSeats(ticket.getNumberOfSeats())
                .booked(ticket.isBooked())
                .passengers(passengers)
                .build();
    }

    private void sendKafkaNotifications(Ticket ticket) {

        for (Integer pid : ticket.getPassengerIds()) {
            try {
                PassengerDetailsResponse passenger =
                        passengerInterface.getPassengerDetails(pid).getBody();

                if (passenger == null) continue;

                TicketBookedEvent event = new TicketBookedEvent(
                        passenger.getEmail(),
                        ticket.getPnr(),
                        ticket.getFlightId(),
                        ticket.getNumberOfSeats()
                );

                kafkaTemplate.send("ticket-confirmation", event);

            } catch (feign.FeignException | org.springframework.kafka.KafkaException ex) {
                logger.error("Kafka failure | pid={} | {}", pid, ex.getMessage());
            }
        }
    }

    public ResponseEntity<SeatMapResponse> getSeatMap(int flightId) {

        FlightResponse flight = flightInterface.getByID(flightId).getBody();

        if (flight == null) {
            throw new ResourceNotFoundException("Flight not found");
        }

        List<String> bookedSeats = ticketRepository.findBookedSeatNumbers(flightId)
                .stream()
                .map(this::normalizeSeat)
                .toList();

        SeatMapResponse response = new SeatMapResponse(
                flightId,
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                bookedSeats
        );

        return ResponseEntity.ok(response);
    }

    private List<String> normalizeSeats(List<String> seatNumbers) {
        return seatNumbers.stream()
                .map(this::normalizeSeat)
                .toList();
    }

    private String normalizeSeat(String seat) {
        if (seat == null) {
            throw new IllegalArgumentException("Seat number cannot be null");
        }
        return seat.trim().toUpperCase();
    }

    private void validateSeatNumbers(List<String> seatNumbers, int totalSeats) {

        Set<String> unique = new HashSet<>(seatNumbers);

        if (unique.size() != seatNumbers.size()) {
            throw new IllegalArgumentException("Duplicate seat numbers are not allowed");
        }

        for (String seat : seatNumbers) {
            try {
                int seatNumber = Integer.parseInt(seat);
                if (seatNumber < 1 || seatNumber > totalSeats) {
                    throw new IllegalArgumentException(
                            "Seat number " + seat + " is out of range (1-" + totalSeats + ")");
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Seat numbers must be numeric");
            }
        }
    }
}
