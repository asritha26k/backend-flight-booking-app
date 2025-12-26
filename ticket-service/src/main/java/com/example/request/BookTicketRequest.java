package com.example.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class BookTicketRequest {

    @NotNull
    private Integer flightId;

    @NotEmpty
    private List<Integer> passengerIds;

}
