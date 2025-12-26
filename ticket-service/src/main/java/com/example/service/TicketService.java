package com.example.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<String> bookTicketService(BookTicketRequest req) {

        if (req.getPassengerIds() == null || req.getPassengerIds().isEmpty()) {
            throw new IllegalArgumentException("Passenger list cannot be empty");
        }

        int seats = req.getPassengerIds().size();
        flightInterface.reserveSeats(req.getFlightId(), seats);

        String pnr = UUID.randomUUID().toString();

        Ticket ticket = Ticket.builder()
                .pnr(pnr)
                .flightId(req.getFlightId())
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

            } catch (Exception ex) {
                logger.error("Kafka failure | pid={} | {}", pid, ex.getMessage());
            }
        }
    }
}
