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
import com.aasismgt.repository.UserRepository;
import com.aasismgt.entities.User;
import com.aasismgt.enums.Roles;
import java.util.List;
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

    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
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

        User user = getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (user.getRole() != Roles.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can delete tickets"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        ticket.setGeneralStatus(GeneralStatus.DELETED);
        ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success("Ticket deleted successfully", null)
        );
    }


    @Override
    public ResponseEntity<ApiResponse> getTickets() {

        User user = getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        List<Ticket> tickets;

        if (user.getRole() == Roles.ADMIN) {

            tickets = ticketRepository.findByGeneralStatus(
                    GeneralStatus.ACTIVE);

        } else if (user.getRole() == Roles.EMPLOYEE) {

            tickets = ticketRepository.findByGeneralStatusAndAssignedTo(
                    GeneralStatus.ACTIVE, user.getId());

        } else if (user.getRole() == Roles.REVIEWER) {

            tickets = ticketRepository.findByGeneralStatusAndProgressStatus(
                    GeneralStatus.ACTIVE, ProgressStatus.IN_REVIEW);

        } else {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("You are not authorized to view tickets"));
        }

        List<TicketResponse> ticketResponses = tickets.stream()
                .map(TicketResponse::from)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse().success("Tickets retrieved successfully", ticketResponses)
        );
    }


    @Override
    public ResponseEntity<ApiResponse> getTicket(Long id) {

        User user = getCurrentUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        boolean canView = false;

        if (user.getRole() == Roles.ADMIN) {

            canView = true;

        } else if (user.getRole() == Roles.EMPLOYEE) {

            canView = ticket.getAssignedTo() != null
                    && ticket.getAssignedTo().equals(user.getId());

        } else if (user.getRole() == Roles.REVIEWER) {

            canView = ticket.getProgressStatus() == ProgressStatus.IN_REVIEW;
        }

        if (!canView) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket retrieved successfully",
                        TicketResponse.from(ticket)
                )
        );
    }


    //
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .orElse(null);
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
