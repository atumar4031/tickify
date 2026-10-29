package com.aasismgt.service;

import com.aasismgt.reponse.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface TicketService {
    ResponseEntity<ApiResponse> createTicket(Object requesr);
    ResponseEntity<ApiResponse> updateTicket(Object requesr);
    ResponseEntity<ApiResponse> deleteTicket(Long id);
    ResponseEntity<ApiResponse> getTicket(Long id);
    Page<Object> getTickets();
}
