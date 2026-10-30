package com.aasismgt.entities;

import com.aasismgt.enums.AssignmentStatus;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus assignmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus progressStatus;

    private Long assignedTo;

    private Instant createdAt;

    private Instant completedAt;

    private Instant assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeneralStatus generalStatus;
}


// Ticket
//
//Data Types Field Name
//
//(Long) -> Id
//(String) -> title
//(TEXT) -> description
//(enum) -> assignnemt status (ASSIGNED, UNASSIGNED)
//(enum) -> progress status (OPEN, IN_PROGRESS, IN_REVIEW, DONE)
//(Long) -> assignedTo (user_id)
//(Instant) -> createdAt
//(Instant) -> completedAt
//(Instant) -> assignedAt
//(enum) -> status (ACTIVE, INACTIVE, DELETED)
//
//Note: please refer to user entity if you got stock and need some idea

