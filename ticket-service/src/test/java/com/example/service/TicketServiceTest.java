package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.event.TicketBookedEvent;
import com.example.model.Ticket;
import com.example.repository.TicketRepository;
import com.example.request.BookTicketRequest;
import com.example.response.FlightResponse;
import com.example.response.PassengerDetailsResponse;
import com.example.response.SeatMapResponse;
import com.example.response.Airline;

import com.example.feign.FlightInterface;
import com.example.feign.PassengerInterface;

public class TicketServiceTest {

    @Test
    void testBookTicketService_success() {
        TicketRepository repo = mock(TicketRepository.class);
        PassengerInterface passengerFeign = mock(PassengerInterface.class);
        FlightInterface flightFeign = mock(FlightInterface.class);
        KafkaTemplate<String, TicketBookedEvent> kafka = mock(KafkaTemplate.class);

        TicketService svc = new TicketService(repo, passengerFeign, flightFeign, kafka);

        BookTicketRequest req = new BookTicketRequest(1, List.of(1, 2), List.of("1", "2"));

        FlightResponse flight = new FlightResponse(1, Airline.INDIGO, "NYC", "LAX", 100.0,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(6), 100, 100);

        when(flightFeign.getByID(1)).thenReturn(ResponseEntity.ok(flight));
        when(repo.findBookedSeatNumbers(1)).thenReturn(List.of());

        // ensure passenger details are returned so sendKafkaNotifications won't NPE
        when(passengerFeign.getPassengerDetails(1)).thenReturn(ResponseEntity.ok(new PassengerDetailsResponse("P1", "p1@example.com", "")));
        when(passengerFeign.getPassengerDetails(2)).thenReturn(ResponseEntity.ok(new PassengerDetailsResponse("P2", "p2@example.com", "")));

        // when saving, return ticket with pnr set
        when(repo.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            // emulate generated id
            return Ticket.builder()
                    .pnr(t.getPnr())
                    .flightId(t.getFlightId())
                    .seatNumbers(t.getSeatNumbers())
                    .passengerIds(t.getPassengerIds())
                    .numberOfSeats(t.getNumberOfSeats())
                    .booked(t.isBooked())
                    .build();
        });

        // reserveSeats should do nothing (void)
        doNothing().when(flightFeign).reserveSeats(1, 2);

        ResponseEntity<String> resp = svc.bookTicketService(req);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        // saved pnr should be returned
        assertFalse(resp.getBody().isBlank());

        verify(repo, times(1)).save(any(Ticket.class));
        verify(flightFeign, times(1)).reserveSeats(1, 2);
    }

    @Test
    void testBookTicketService_conflictSeats_throws() {
        TicketRepository repo = mock(TicketRepository.class);
        PassengerInterface passengerFeign = mock(PassengerInterface.class);
        FlightInterface flightFeign = mock(FlightInterface.class);
        KafkaTemplate<String, TicketBookedEvent> kafka = mock(KafkaTemplate.class);

        TicketService svc = new TicketService(repo, passengerFeign, flightFeign, kafka);

        BookTicketRequest req = new BookTicketRequest(1, List.of(1), List.of("1"));

        FlightResponse flight = new FlightResponse(1, Airline.AIRINDIA, "NYC", "LAX", 120.0,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(6), 100, 100);

        when(flightFeign.getByID(1)).thenReturn(ResponseEntity.ok(flight));
        when(repo.findBookedSeatNumbers(1)).thenReturn(List.of("1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> svc.bookTicketService(req));
        assertTrue(ex.getMessage().contains("Seats already booked"));
    }

    @Test
    void testGetByPnrService_success() {
        TicketRepository repo = mock(TicketRepository.class);
        PassengerInterface passengerFeign = mock(PassengerInterface.class);
        FlightInterface flightFeign = mock(FlightInterface.class);
        KafkaTemplate<String, TicketBookedEvent> kafka = mock(KafkaTemplate.class);

        TicketService svc = new TicketService(repo, passengerFeign, flightFeign, kafka);

        Ticket t = Ticket.builder()
                .pnr("PNR123")
                .flightId(5)
                .seatNumbers(List.of("1","2"))
                .passengerIds(List.of(10,11))
                .numberOfSeats(2)
                .booked(true)
                .build();

        when(repo.findByPnr("PNR123")).thenReturn(Optional.of(t));

        FlightResponse flight = new FlightResponse(5, Airline.EMIRATES, "A", "B", 50.0,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 50, 48);
        when(flightFeign.getByID(5)).thenReturn(ResponseEntity.ok(flight));

        when(passengerFeign.getPassengerDetails(10)).thenReturn(ResponseEntity.ok(new PassengerDetailsResponse("Alice", "alice@example.com", "")));
        when(passengerFeign.getPassengerDetails(11)).thenReturn(ResponseEntity.ok(new PassengerDetailsResponse("Bob", "bob@example.com", "")));

        var resp = svc.getByPnrService("PNR123");
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("PNR123", resp.getBody().getPnr());
        assertEquals(2, resp.getBody().getPassengers().size());
    }

    @Test
    void testGetSeatMap_flightNotFound_throws() {
        TicketRepository repo = mock(TicketRepository.class);
        PassengerInterface passengerFeign = mock(PassengerInterface.class);
        FlightInterface flightFeign = mock(FlightInterface.class);
        KafkaTemplate<String, TicketBookedEvent> kafka = mock(KafkaTemplate.class);

        TicketService svc = new TicketService(repo, passengerFeign, flightFeign, kafka);

        when(flightFeign.getByID(99)).thenReturn(ResponseEntity.ok(null));

        assertThrows(RuntimeException.class, () -> svc.getSeatMap(99));
    }

    @Test
    void testDeleteTicketById_cannotCancelWithin24Hours_returnsBadRequest() {
        TicketRepository repo = mock(TicketRepository.class);
        PassengerInterface passengerFeign = mock(PassengerInterface.class);
        FlightInterface flightFeign = mock(FlightInterface.class);
        KafkaTemplate<String, TicketBookedEvent> kafka = mock(KafkaTemplate.class);

        TicketService svc = new TicketService(repo, passengerFeign, flightFeign, kafka);

        Ticket t = Ticket.builder()
                .pnr("PNR1")
                .flightId(7)
                .seatNumbers(List.of("1"))
                .passengerIds(List.of(2))
                .numberOfSeats(1)
                .booked(true)
                .build();

        // repository findById returns ticket with id
        when(repo.findById(123)).thenReturn(Optional.of(t));

        // flight departure in 10 hours -> cannot cancel
        FlightResponse flight = new FlightResponse(7, Airline.INDIGO, "X", "Y", 75.0,
                LocalDateTime.now().plusHours(10), LocalDateTime.now().plusHours(13), 100, 99);
        when(flightFeign.getByID(7)).thenReturn(ResponseEntity.ok(flight));

        var resp = svc.deleteTicketById(123);
        assertEquals(400, resp.getStatusCodeValue());
        assertTrue(resp.getBody().contains("cannot be cancelled"));
    }
}
