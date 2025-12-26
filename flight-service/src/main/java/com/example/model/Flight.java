package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Flight {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private int flightId;
	private Airline airline;
	private String origin;
	private String destination;
	private double price;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;
	private int totalSeats;
	private int availableSeats;

	public Flight() {}

	@SuppressWarnings("java:S107")
	public Flight(int flightId, Airline airline, String origin, String destination, double price,
				  LocalDateTime departureTime, LocalDateTime arrivalTime,
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
	public void setFlightId(int flightId) { this.flightId = flightId; }

	public Airline getAirline() { return airline; }
	public void setAirline(Airline airline) { this.airline = airline; }

	public String getOrigin() { return origin; }
	public void setOrigin(String origin) { this.origin = origin; }

	public String getDestination() { return destination; }
	public void setDestination(String destination) { this.destination = destination; }

	public double getPrice() { return price; }
	public void setPrice(double price) { this.price = price; }

	public LocalDateTime getDepartureTime() { return departureTime; }
	public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

	public LocalDateTime getArrivalTime() { return arrivalTime; }
	public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

	public int getTotalSeats() { return totalSeats; }
	public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

	public int getAvailableSeats() { return availableSeats; }
	public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
