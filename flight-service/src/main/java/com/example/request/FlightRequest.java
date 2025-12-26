package com.example.request;

import java.time.LocalDateTime;

import com.example.model.Airline;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FlightRequest {

	@NotNull
	private Airline airline;
	@NotNull
	private String origin;
	@NotNull
	private String destination;
	@Positive
	private double price;
	@NotNull
	@Future
	private LocalDateTime departureTime;
	@NotNull
	@Future
	private LocalDateTime arrivalTime;
	@NotNull
	@Positive
	private int totalSeats;

		public FlightRequest() {}

		public FlightRequest(Airline airline, String origin, String destination, double price,
												 LocalDateTime departureTime, LocalDateTime arrivalTime, int totalSeats) {
			this.airline = airline;
			this.origin = origin;
			this.destination = destination;
			this.price = price;
			this.departureTime = departureTime;
			this.arrivalTime = arrivalTime;
			this.totalSeats = totalSeats;
		}

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

}
