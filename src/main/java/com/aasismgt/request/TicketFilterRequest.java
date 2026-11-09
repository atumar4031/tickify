package com.aasismgt.request;

import com.aasismgt.enums.AssignmentStatus;
import com.aasismgt.enums.ProgressStatus;
import com.aasismgt.enums.TitleMatch;

import java.time.LocalDate;

public record TicketFilterRequest(
        ProgressStatus progressStatus,
        AssignmentStatus assignmentStatus,
        LocalDate assignedFrom,
        LocalDate assignedTo,
        String title,
        TitleMatch titleMatch
) {
}
