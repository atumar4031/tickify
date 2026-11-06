package com.aasismgt.reponse;

import com.aasismgt.entities.Ticket;
import com.aasismgt.enums.AssignmentStatus;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private AssignmentStatus assignmentStatus;
    private ProgressStatus progressStatus;
    private Long assignedTo;
    private String createdAt;
    private String completedAt;
    private String assignedAt;
    private GeneralStatus generalStatus;

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getAssignmentStatus(),
                ticket.getProgressStatus(),
                ticket.getAssignedTo(),
                ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : null,
                ticket.getCompletedAt() != null ? ticket.getCompletedAt().toString() : null,
                ticket.getAssignedAt() != null ? ticket.getAssignedAt().toString() : null,
                ticket.getGeneralStatus());
    }
}