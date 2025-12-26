package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
