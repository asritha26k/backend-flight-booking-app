package com.example.request;

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
	private int flightId;
	@NotNull
	private Integer [] passengerIds;
//	@NotNull
//	private String seatNo;
	@NotNull
	private int numberOfSeats;
}
