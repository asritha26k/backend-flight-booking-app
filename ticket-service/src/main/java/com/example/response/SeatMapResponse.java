package com.example.response;

import java.util.List;

public class SeatMapResponse {

    private int flightId;
    private int totalSeats;
    private int availableSeats;
    private List<String> bookedSeats;

    public SeatMapResponse() {}

    public SeatMapResponse(int flightId, int totalSeats, int availableSeats, List<String> bookedSeats) {
        this.flightId = flightId;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.bookedSeats = bookedSeats;
    }

    public int getFlightId() { return flightId; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public List<String> getBookedSeats() { return bookedSeats; }
}
