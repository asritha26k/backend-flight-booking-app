package com.example.response;

import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {

    private Integer id;
    private String pnr;

    private String origin;
    private String destination;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private int numberOfSeats;
    private boolean booked;

    private List<PassengerDetailsResponse> passengers;

    public TicketResponse() {}

    public TicketResponse(Integer id, String pnr, String origin, String destination,
                          LocalDateTime departureTime, LocalDateTime arrivalTime,
                          int numberOfSeats, boolean booked,
                          List<PassengerDetailsResponse> passengers) {
        this.id = id;
        this.pnr = pnr;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.numberOfSeats = numberOfSeats;
        this.booked = booked;
        this.passengers = passengers;
    }

    public Integer getId() { return id; }
    public String getPnr() { return pnr; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public boolean isBooked() { return booked; }
    public List<PassengerDetailsResponse> getPassengers() { return passengers; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer id;
        private String pnr;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private int numberOfSeats;
        private boolean booked;
        private List<PassengerDetailsResponse> passengers;

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder pnr(String pnr) { this.pnr = pnr; return this; }
        public Builder origin(String origin) { this.origin = origin; return this; }
        public Builder destination(String destination) { this.destination = destination; return this; }
        public Builder departureTime(LocalDateTime departureTime) { this.departureTime = departureTime; return this; }
        public Builder arrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; return this; }
        public Builder numberOfSeats(int seats) { this.numberOfSeats = seats; return this; }
        public Builder booked(boolean booked) { this.booked = booked; return this; }
        public Builder passengers(List<PassengerDetailsResponse> passengers) { this.passengers = passengers; return this; }

        public TicketResponse build() {
            return new TicketResponse(id, pnr, origin, destination, departureTime, arrivalTime,
                                      numberOfSeats, booked, passengers);
        }
    }
}
