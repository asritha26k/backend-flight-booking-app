package com.example.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BookTicketRequest {

    @NotNull
    private Integer flightId;

    @NotEmpty
    private List<Integer> passengerIds;

    public BookTicketRequest() {}

    public BookTicketRequest(Integer flightId, List<Integer> passengerIds) {
        this.flightId = flightId;
        this.passengerIds = passengerIds;
    }

    public Integer getFlightId() { return flightId; }
    public List<Integer> getPassengerIds() { return passengerIds; }
}
