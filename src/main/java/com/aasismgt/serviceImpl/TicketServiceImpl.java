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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        boolean isUser = user.getRole() == Roles.EMPLOYEE;
        boolean isAsseigned = ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(user.getId());
        if (isUser && !isAsseigned) {
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

    @Override
    public Page<TicketResponse> getTickets(int page, int size) {

        User user = getCurrentUser();

        if (user == null) {
           throw new RuntimeException("User not authenticated");
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Ticket> tickets =  getPaginatedTicket(pageRequest, user);
        return tickets.map(TicketResponse::from);
    }


    private Page<Ticket> getPaginatedTicket(PageRequest pageRequest, User user) {
        return switch (user.getRole()) {
            case ADMIN -> ticketRepository.findAll(pageRequest);
            case EMPLOYEE -> ticketRepository.findAllByAssignedToAndGeneralStatus(user.getId(), GeneralStatus.ACTIVE, pageRequest);
            case REVIEWER -> ticketRepository.findByGeneralStatusAndProgressStatus(GeneralStatus.ACTIVE, ProgressStatus.IN_REVIEW, pageRequest);
            default -> throw new RuntimeException("You are not authorized to view tickets");
        };
    }

//        List<Ticket> tickets;
//        if (user.getRole() == Roles.ADMIN) {
//            tickets = ticketRepository.findByGeneralStatus(GeneralStatus.ACTIVE);
//        } else if (user.getRole() == Roles.EMPLOYEE) {
//            tickets = ticketRepository.findByGeneralStatusAndAssignedTo(GeneralStatus.ACTIVE, user.getId());
//        } else if (user.getRole() == Roles.REVIEWER) {
//            tickets = ticketRepository.findByGeneralStatusAndProgressStatus(
//                    GeneralStatus.ACTIVE, ProgressStatus.IN_REVIEW);
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse().error("You are not authorized to view tickets"));
//        }
//
//        List<TicketResponse> ticketResponses = tickets.stream()
//                .map(TicketResponse::from)
//                .toList();
//
//        return ResponseEntity.ok(
//                new ApiResponse().success("Tickets retrieved successfully", ticketResponses)
//        );
//    }

    @Override
    public ResponseEntity<ApiResponse> assignTicket(Long id, Long userId) {

        User admin = getCurrentUser();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (admin.getRole() != Roles.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can assign tickets"));
        }

        if (id == null || userId == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID and employee ID are required"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        User employee = userRepository.findById(userId).orElse(null);

        if (employee == null || employee.getRole() != Roles.EMPLOYEE
                || employee.getStatus() != GeneralStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Active employee not found"));
        }

        if (ticket.getAssignedTo() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse().error(
                            "Ticket is already assigned. Unassign it first."
                    ));
        }

        ticket.setAssignedTo(employee.getId());
        ticket.setAssignmentStatus(AssignmentStatus.ASSIGNED);
        ticket.setAssignedAt(Instant.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket assigned successfully",
                        TicketResponse.from(savedTicket)
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponse> unAssignTicket(Long id, Long userId) {

        User admin = getCurrentUser();

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (admin.getRole() != Roles.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only admins can unassign tickets"));
        }

        if (id == null || userId == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID and employee ID are required"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error(
                            "This ticket is not assigned to the specified employee"
                    ));
        }

        ticket.setAssignedTo(null);
        ticket.setAssignmentStatus(AssignmentStatus.UNASSIGNED);
        ticket.setAssignedAt(null);

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket unassigned successfully",
                        TicketResponse.from(savedTicket)
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponse> moveTicket(Long id) {

        User employee = getCurrentUser();

        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (employee.getRole() != Roles.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error("Only employees can move tickets"));
        }

        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID is required"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error(
                            "You can only move tickets assigned to you"
                    ));
        }

        if (ticket.getProgressStatus() != ProgressStatus.OPEN) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error(
                            "Only OPEN tickets can be moved to IN_PROGRESS"
                    ));
        }

        ticket.setProgressStatus(ProgressStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket moved to IN_PROGRESS successfully",
                        TicketResponse.from(savedTicket)
                )
        );
    }
    @Override
    public ResponseEntity<ApiResponse> submitReview(Long id) {

        User employee = getCurrentUser();

        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (employee.getRole() != Roles.EMPLOYEE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error(
                            "Only employees can submit tickets for review"
                    ));
        }

        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID is required"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error(
                            "You can only submit tickets assigned to you"
                    ));
        }

        if (ticket.getProgressStatus() != ProgressStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error(
                            "Only IN_PROGRESS tickets can be submitted for review"
                    ));
        }

        ticket.setProgressStatus(ProgressStatus.IN_REVIEW);

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket submitted for review successfully",
                        TicketResponse.from(savedTicket)
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponse> approveReview(Long id) {

        User reviewer = getCurrentUser();

        if (reviewer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse().error("Please log in"));
        }

        if (reviewer.getRole() != Roles.REVIEWER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse().error(
                            "Only reviewers can approve tickets"
                    ));
        }

        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error("Ticket ID is required"));
        }

        Ticket ticket = ticketRepository
                .findByIdAndGeneralStatus(id, GeneralStatus.ACTIVE)
                .orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse().error("Ticket not found"));
        }

        if (ticket.getProgressStatus() != ProgressStatus.IN_REVIEW) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse().error(
                            "Only IN_REVIEW tickets can be approved"
                    ));
        }

        ticket.setProgressStatus(ProgressStatus.COMPLETED);
        ticket.setCompletedAt(Instant.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return ResponseEntity.ok(
                new ApiResponse().success(
                        "Ticket approved and completed successfully",
                        TicketResponse.from(savedTicket)
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
