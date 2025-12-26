package com.example.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ticketId;

    @Column(nullable = false, unique = true)
    private String pnr;

    @Column(nullable = false)
    private Integer flightId;

            @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(
            name = "ticket_seats",
            joinColumns = @JoinColumn(name = "ticket_id")
        )
        @Column(name = "seat_number")
            private List<String> seatNumbers = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "ticket_passengers",
            joinColumns = @JoinColumn(name = "ticket_id")
    )
    @Column(name = "passenger_id")
    private List<Integer> passengerIds;

    @Column(nullable = false)
    private int numberOfSeats;

    @Column(nullable = false)
    private boolean booked;

    public Ticket() {}

    public Ticket(Integer ticketId, String pnr, Integer flightId, List<String> seatNumbers,
                  List<Integer> passengerIds, int numberOfSeats, boolean booked) {
        this.ticketId = ticketId;
        this.pnr = pnr;
        this.flightId = flightId;
        this.seatNumbers = seatNumbers == null ? new java.util.ArrayList<>() : seatNumbers;
        this.passengerIds = passengerIds;
        this.numberOfSeats = numberOfSeats;
        this.booked = booked;
    }

    public Integer getTicketId() { return ticketId; }
    public String getPnr() { return pnr; }
    public Integer getFlightId() { return flightId; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public List<Integer> getPassengerIds() { return passengerIds; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String pnr;
        private Integer flightId;
        private List<String> seatNumbers;
        private List<Integer> passengerIds;
        private int numberOfSeats;
        private boolean booked;

        public Builder pnr(String pnr) { this.pnr = pnr; return this; }
        public Builder flightId(Integer flightId) { this.flightId = flightId; return this; }
        public Builder seatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; return this; }
        public Builder passengerIds(List<Integer> passengerIds) { this.passengerIds = passengerIds; return this; }
        public Builder numberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; return this; }
        public Builder booked(boolean booked) { this.booked = booked; return this; }

        public Ticket build() {
            return new Ticket(null, pnr, flightId,
                    seatNumbers == null ? new java.util.ArrayList<>() : seatNumbers,
                    passengerIds, numberOfSeats, booked);
        }
    }
}
