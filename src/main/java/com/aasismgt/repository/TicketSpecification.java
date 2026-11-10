package com.aasismgt.repository;

import com.aasismgt.entities.Ticket;
import com.aasismgt.entities.User;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.Roles;
import com.aasismgt.enums.TitleMatch;
import com.aasismgt.request.TicketFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    private static final ZoneId NIGERIA_ZONE =
            ZoneId.of("Africa/Lagos");

    public static Specification<Ticket> filterTickets(
            TicketFilterRequest filterRequest, User user) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Only show active tickets
            predicates.add(
                    criteriaBuilder.equal(
                            root.get("generalStatus"),
                            GeneralStatus.ACTIVE
                    )
            );

           // Apply ticket visibility based on user role
            if (user.getRole() == Roles.EMPLOYEE) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assignedTo"),
                                user.getId()
                        )
                );
            }

            if (user.getRole() != Roles.ADMIN
                    && (filterRequest.assignmentStatus() != null
                    || filterRequest.assignedFrom() != null
                    || filterRequest.assignedTo() != null)) {

                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Only admins can filter by assignment status and assignment date"
                );
            }

            // Filter by progress status
            if (filterRequest.progressStatus() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("progressStatus"),
                                filterRequest.progressStatus()
                        )
                );
            }

            // Filter by assignment status
            if (filterRequest.assignmentStatus() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assignmentStatus"),
                                filterRequest.assignmentStatus()
                        )
                );
            }

            // Filter by assignment start date
            if (filterRequest.assignedFrom() != null) {

                Instant startDate = filterRequest.assignedFrom()
                        .atStartOfDay(NIGERIA_ZONE)
                        .toInstant();

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("assignedAt"),
                                startDate
                        )
                );
            }

            // Filter by assignment end date (inclusive)
            if (filterRequest.assignedTo() != null) {

                Instant endDate = filterRequest.assignedTo()
                        .plusDays(1)
                        .atStartOfDay(NIGERIA_ZONE)
                        .toInstant();

                predicates.add(
                        criteriaBuilder.lessThan(
                                root.get("assignedAt"),
                                endDate
                        )
                );
            }

            // Search by ticket title
            if (filterRequest.title() != null
                    && !filterRequest.title().isBlank()) {

                String searchText = filterRequest.title()
                        .trim()
                        .toLowerCase();

                TitleMatch titleMatch = filterRequest.titleMatch();

                if (titleMatch == null) {
                    titleMatch = TitleMatch.CONTAINS;
                }

                String pattern = switch (titleMatch) {
                    case STARTS_WITH -> searchText + "%";
                    case ENDS_WITH -> "%" + searchText;
                    case CONTAINS -> "%" + searchText + "%";
                };

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.<String>get("title")),
                                pattern
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}