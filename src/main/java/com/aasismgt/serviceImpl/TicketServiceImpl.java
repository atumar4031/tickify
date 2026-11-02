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
import org.springframework.data.domain.Page;
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
                !hastText(ticketRequest.title()) ||
                !hastText(ticketRequest.description())) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket title and description are required"));
        }

        Ticket ticket = new Ticket();

        ticket.setTitle(ticketRequest.title());
        ticket.setDescription(ticketRequest.description());
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
        boolean admin = isAdmin();
        if (!admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can update tickets"));
        }

        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID is required"));
        }

        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        if (ticketRequest == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket title and description are required"));
        }

        boolean hasUpdate = false;
        if (hastText(ticketRequest.title()) && ticketRequest.title().equalsIgnoreCase(ticket.getTitle())) {
            ticket.setTitle(ticketRequest.title());
            hasUpdate = true;
        }

        if (hastText(ticketRequest.description()) && ticketRequest.title().equalsIgnoreCase(ticket.getDescription())) {
            ticket.setDescription(ticketRequest.description());
            hasUpdate = true;
        }

        if (hasUpdate) {
             ticketRepository.save(ticket);
        }


        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket updated successfully",
                        TicketResponse.from(ticket)
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponse> deleteTicket(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> getTicket(Long id) {
        // TicketResponse
        return null;
    }

    @Override
    public Page<TicketResponse> getTickets() {
        return null;
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

    private boolean hastText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
