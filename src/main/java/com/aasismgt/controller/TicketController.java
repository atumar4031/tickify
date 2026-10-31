package com.aasismgt.controller;


import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.request.TicketRequest;
import com.aasismgt.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createTicket(
            @RequestBody TicketRequest ticketRequest) {

        return ticketService.createTicket(ticketRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequest ticketRequest) {

        return ticketService.updateTicket(id, ticketRequest);
    }
}

