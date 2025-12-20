package com.example.demo.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.service.EmailService;
import com.example.event.TicketBookedEvent;

@Service
public class EmailListener {

    @Autowired
    private EmailService emailService;
    @KafkaListener(topics = "${kafka.topic.ticket-booked}", groupId = "email-group")
    public void listen(TicketBookedEvent event) {

        emailService.sendBookingEmail(
            event.getEmail(),
            "Your ticket is booked.\nPNR: " + event.getPnr()
        );
    }

}
