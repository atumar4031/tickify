package com.aasismgt.serviceImpl;

import com.aasismgt.entities.Ticket;
import com.aasismgt.enums.AssignmentStatus;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.reponse.TicketResponse;
import com.aasismgt.repository.TicketRepository;
import com.aasismgt.request.TicketRequest;
import com.aasismgt.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public ResponseEntity<ApiResponse> createTicket(TicketRequest ticketRequest) {

        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can create tickets"));
        }

        if (ticketRequest == null ||
                isBlank(ticketRequest.title()) ||
                isBlank(ticketRequest.description())) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket title and description are required"));
        }

        Ticket ticket = new Ticket();

        ticket.setTitle(ticketRequest.title().trim());
        ticket.setDescription(ticketRequest.description().trim());
        ticket.setAssignmentStatus(AssignmentStatus.UNASSIGNED);
        ticket.setProgressStatus(ProgressStatus.OPEN);
        ticket.setGeneralStatus(GeneralStatus.ACTIVE);
        ticket.setCreatedAt(Instant.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse().success(
                        "Ticket created successfully",
                        TicketResponse.from(savedTicket)
                ));
    }

    @Override
    public ResponseEntity<ApiResponse> updateTicket(Long id, TicketRequest ticketRequest) {

        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can update tickets"));
        }

        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID is required"));
        }

        if (ticketRequest == null ||
                isBlank(ticketRequest.title()) ||
                isBlank(ticketRequest.description())) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket title and description are required"));
        }

        Ticket ticket = ticketRepository.findById(id).orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        ticket.setTitle(ticketRequest.title().trim());
        ticket.setDescription(ticketRequest.description().trim());

        Ticket updatedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket updated successfully",
                        TicketResponse.from(updatedTicket)
                )
        );
    }

    private boolean isAdmin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
