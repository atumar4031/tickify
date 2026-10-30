package com.aasismgt.serviceImpl;

import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    @Override
    public ResponseEntity<ApiResponse> createTicket(Object requesr) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> updateTicket(Object requesr) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> deleteTicket(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> getTicket(Long id) {
        return null;
    }

    @Override
    public Page<Object> getTickets() {
        return null;
    }
}
