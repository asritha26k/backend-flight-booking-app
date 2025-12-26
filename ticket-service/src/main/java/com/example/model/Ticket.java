package com.example.model;

import jakarta.persistence.*;

import java.util.List;

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

    public Ticket(Integer ticketId, String pnr, Integer flightId, List<Integer> passengerIds,
                  int numberOfSeats, boolean booked) {
        this.ticketId = ticketId;
        this.pnr = pnr;
        this.flightId = flightId;
        this.passengerIds = passengerIds;
        this.numberOfSeats = numberOfSeats;
        this.booked = booked;
    }

    public Integer getTicketId() { return ticketId; }
    public String getPnr() { return pnr; }
    public Integer getFlightId() { return flightId; }
    public List<Integer> getPassengerIds() { return passengerIds; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String pnr;
        private Integer flightId;
        private List<Integer> passengerIds;
        private int numberOfSeats;
        private boolean booked;

        public Builder pnr(String pnr) { this.pnr = pnr; return this; }
        public Builder flightId(Integer flightId) { this.flightId = flightId; return this; }
        public Builder passengerIds(List<Integer> passengerIds) { this.passengerIds = passengerIds; return this; }
        public Builder numberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; return this; }
        public Builder booked(boolean booked) { this.booked = booked; return this; }

        public Ticket build() {
            return new Ticket(null, pnr, flightId, passengerIds, numberOfSeats, booked);
        }
    }
}
