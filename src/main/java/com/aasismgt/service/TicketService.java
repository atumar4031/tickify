package com.aasismgt.service;

import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.reponse.TicketResponse;
import com.aasismgt.request.TicketFilterRequest;
import com.aasismgt.request.TicketRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface TicketService {
    ResponseEntity<ApiResponse> createTicket(TicketRequest ticketRequest);
    ResponseEntity<ApiResponse> updateTicket(Long id, TicketRequest ticketRequest);
    ResponseEntity<ApiResponse> deleteTicket(Long id);
    ResponseEntity<ApiResponse> getTicket(Long id);
    Page<TicketResponse> getTickets(int page, int size, TicketFilterRequest filterRequest);
    ResponseEntity<ApiResponse> assignTicket(Long id, Long userId); // admin
    ResponseEntity<ApiResponse> unAssignTicket(Long id, Long userId); // admin
    ResponseEntity<ApiResponse> moveTicket(Long id); // employee uses this to move ticket to next stage
    ResponseEntity<ApiResponse> submitReview(Long id); // employee uses this to submit ticket for review
    ResponseEntity<ApiResponse> approveReview(Long id); // reviewer uses this to approve review

}
