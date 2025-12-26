package com.example.response;

import java.time.LocalDateTime;

@SuppressWarnings("java:S107")
public class FlightResponse {
	private int flightId;
	private Airline airline;
	private String origin;
	private String destination;
	private double price;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;
	private int totalSeats;
	private int availableSeats;

	public FlightResponse() {}

	public FlightResponse(int flightId, Airline airline, String origin, String destination,
					  double price, LocalDateTime departureTime, LocalDateTime arrivalTime,
					  int totalSeats, int availableSeats) {
		this.flightId = flightId;
		this.airline = airline;
		this.origin = origin;
		this.destination = destination;
		this.price = price;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.totalSeats = totalSeats;
		this.availableSeats = availableSeats;
	}

	public int getFlightId() { return flightId; }
	public Airline getAirline() { return airline; }
	public String getOrigin() { return origin; }
	public String getDestination() { return destination; }
	public double getPrice() { return price; }
	public LocalDateTime getDepartureTime() { return departureTime; }
	public LocalDateTime getArrivalTime() { return arrivalTime; }
	public int getTotalSeats() { return totalSeats; }
	public int getAvailableSeats() { return availableSeats; }
}