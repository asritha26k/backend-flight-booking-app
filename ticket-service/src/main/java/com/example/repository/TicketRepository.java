package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    Optional<Ticket> findByPnr(String pnr);

    @Query("""
        SELECT t FROM Ticket t
        WHERE :pid MEMBER OF t.passengerIds
    """)
    List<Ticket> findAllByPassengerId(@Param("pid") Integer passengerId);

    @Query("""
        SELECT seat
        FROM Ticket t
        JOIN t.seatNumbers seat
        WHERE t.flightId = :flightId AND t.booked = true
    """)
    List<String> findBookedSeatNumbers(@Param("flightId") Integer flightId);
}

