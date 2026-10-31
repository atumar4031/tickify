package com.aasismgt.service;

import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.request.TicketRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface TicketService {
    ResponseEntity<ApiResponse> createTicket(TicketRequest ticketRequest);
    ResponseEntity<ApiResponse> updateTicket(Long id, TicketRequest ticketRequest);
}
