package com.example.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class BookTicketRequest {

    @NotNull
    private Integer flightId;

    @NotEmpty
    private List<Integer> passengerIds;

    @NotEmpty
    private List<String> seatNumbers;

    public BookTicketRequest() {}

    public BookTicketRequest(Integer flightId, List<Integer> passengerIds, List<String> seatNumbers) {
        this.flightId = flightId;
        this.passengerIds = passengerIds;
        this.seatNumbers = seatNumbers;
    }

    public Integer getFlightId() { return flightId; }
    public List<Integer> getPassengerIds() { return passengerIds; }
    public List<String> getSeatNumbers() { return seatNumbers; }
}
