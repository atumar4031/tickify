package com.aasismgt.controller;


import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.reponse.TicketResponse;
import com.aasismgt.request.TicketRequest;
import com.aasismgt.service.TicketService;
import org.springframework.data.domain.Page;
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

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse> deleteTicket(@PathVariable Long id) {
        return ticketService.deleteTicket(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }


    @GetMapping("/paged")
    public Page<TicketResponse> getTickets(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ticketService.getTickets(page, size);
    }

    //
}

