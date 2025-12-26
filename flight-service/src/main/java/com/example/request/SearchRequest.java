package com.example.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class SearchRequest {
	@NotNull
	private String origin;
	@NotNull
	private String destination;

	@NotNull
	private LocalDateTime departureDateTime;

	public SearchRequest() {}

	public SearchRequest(String origin, String destination, LocalDateTime departureDateTime) {
		this.origin = origin;
		this.destination = destination;
		this.departureDateTime = departureDateTime;
	}

	public String getOrigin() { return origin; }
	public void setOrigin(String origin) { this.origin = origin; }

	public String getDestination() { return destination; }
	public void setDestination(String destination) { this.destination = destination; }

	public LocalDateTime getDepartureDateTime() { return departureDateTime; }
	public void setDepartureDateTime(LocalDateTime departureDateTime) { this.departureDateTime = departureDateTime; }

}
