package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.exception.ResourceNotFoundException;
import com.example.model.Airline;
import com.example.model.Flight;
import com.example.repository.FlightRepository;
import com.example.request.FlightRequest;
import com.example.request.SearchRequest;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

	@Mock
	private FlightRepository flightRepository;

	@InjectMocks
	private FlightService flightService;

	private Flight createFlight() {
		Flight flight = new Flight();
		flight.setFlightId(1);
		flight.setAirline(Airline.INDIGO);
		flight.setOrigin("DEL");
		flight.setDestination("HYD");
		flight.setPrice(5000);
		flight.setDepartureTime(LocalDateTime.now().plusDays(1));
		flight.setArrivalTime(LocalDateTime.now().plusDays(2));
		flight.setTotalSeats(100);
		flight.setAvailableSeats(100);
		return flight;
	}

	private FlightRequest createRequest() {
		FlightRequest req = new FlightRequest();
		req.setAirline(Airline.INDIGO);
		req.setOrigin("DEL");
		req.setDestination("HYD");
		req.setPrice(5000);
		req.setDepartureTime(LocalDateTime.now().plusDays(1));
		req.setArrivalTime(LocalDateTime.now().plusDays(2));
		req.setTotalSeats(100);
		return req;
	}

	@Test
	void testRegisterFlight() {
		Flight flight = createFlight();
		FlightRequest req = createRequest();

		when(flightRepository.save(any(Flight.class))).thenReturn(flight);

		var response = flightService.registerFlightByIDService(req);
		assertEquals(201, response.getStatusCode().value());
		assertEquals(1, response.getBody());
	}

	@Test
	void testGetById_Success() throws Exception {
		Flight flight = createFlight();
		when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

		var response = flightService.getByIDService(1);
		assertEquals(200, response.getStatusCode().value());
		assertEquals("DEL", response.getBody().getOrigin());
	}

	@Test
	void testGetById_NotFound() {
		when(flightRepository.findById(1)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			flightService.getByIDService(1);
		});
	}

	@Test
	void testGetAllFlights() {
		Flight flight1 = createFlight();
		Flight flight2 = createFlight();
		flight2.setFlightId(2);

		when(flightRepository.findAll()).thenReturn(Arrays.asList(flight1, flight2));

		List<Flight> flights = flightService.getAllFlights();
		assertEquals(2, flights.size());
		assertEquals(flight1.getFlightId(), flights.get(0).getFlightId());
		assertEquals(flight2.getFlightId(), flights.get(1).getFlightId());
	}

	@Test
	void testGetByOriginAndDestination() {
		Flight flight = createFlight();
		SearchRequest req = new SearchRequest();
		req.setOrigin("DEL");
		req.setDestination("HYD");

		when(flightRepository.findByOriginAndDestination("DEL", "HYD")).thenReturn(Arrays.asList(flight));

		var response = flightService.getByOriginAndDestinationService(req);
		assertEquals(200, response.getStatusCode().value());
		assertEquals(1, response.getBody().size());
		assertEquals("DEL", response.getBody().get(0).getOrigin());
	}
	void testDelete_Success() throws Exception {
		Flight flight = createFlight();

		when(flightRepository.findById(1)).thenReturn(Optional.of(flight));
		doNothing().when(flightRepository).deleteById(1);

		var response = flightService.deleteByIDService(1);

		assertEquals(200, response.getStatusCode().value());
		assertEquals("deleted", response.getBody());
	}

	@Test
	void testDelete_NotFound() {
		when(flightRepository.findById(1)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			flightService.deleteByIDService(1);
		});
	}
}
