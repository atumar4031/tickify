package com.aasismgt.repository;

import com.aasismgt.enums.AssignmentStatus;
import com.aasismgt.enums.ProgressStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.aasismgt.entities.User;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.Roles;
import com.aasismgt.request.TicketFilterRequest;
import com.aasismgt.reponse.TicketResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public TicketJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<TicketResponse> filterTickets(
            TicketFilterRequest filter,
            User user,
            Pageable pageable) {

        if (user.getRole() != Roles.ADMIN
                && (filter.assignmentStatus() != null
                || filter.assignedFrom() != null
                || filter.assignedTo() != null)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admins can use assignment filters"
            );
        }

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM ticket
            WHERE general_status = ?
            """);

        List<Object> params = buildFilter(filter, user, sql);

        // Pagination
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        // Convert database rows into TicketResponse objects
        RowMapper<TicketResponse> rowMapper = (rs, rowNum) ->
                new TicketResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        AssignmentStatus.valueOf(rs.getString("assignment_status")),
                        ProgressStatus.valueOf(rs.getString("progress_status")),
                        rs.getObject("assigned_to", Long.class),
                        rs.getTimestamp("created_at") != null
                                ? rs.getTimestamp("created_at").toInstant().toString()
                                : null,
                        rs.getTimestamp("completed_at") != null
                                ? rs.getTimestamp("completed_at").toInstant().toString()
                                : null,
                        rs.getTimestamp("assigned_at") != null
                                ? rs.getTimestamp("assigned_at").toInstant().toString()
                                : null,
                        GeneralStatus.valueOf(rs.getString("general_status"))
                );

        List<TicketResponse> content = jdbcTemplate.query(
                sql.toString(),
                rowMapper,
                params.toArray()
        );

        return new PageImpl<>(
                content,
                pageable,
                countTickets(filter, user)
        );
    }

    private long countTickets(TicketFilterRequest filter, User user) {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM ticket
            WHERE general_status = ?
            """);

        List<Object> params = buildFilter(filter, user, sql);

        Long total = jdbcTemplate.queryForObject(
                sql.toString(),
                Long.class,
                params.toArray()
        );

        return total != null ? total : 0L;
    }

    private static List<Object> buildFilter(TicketFilterRequest filter, User user, StringBuilder sql) {
        List<Object> params = new ArrayList<>();

        params.add(GeneralStatus.ACTIVE.name());

        if (user.getRole() == Roles.EMPLOYEE) {
            sql.append(" AND assigned_to = ? ");
            params.add(user.getId());
        }

        if (filter.progressStatus() != null) {
            sql.append(" AND progress_status = ? ");
            params.add(filter.progressStatus().name());
        }

        if (filter.assignmentStatus() != null) {
            sql.append(" AND assignment_status = ? ");
            params.add(filter.assignmentStatus().name());
        }

        if (filter.assignedFrom() != null) {
            Instant startDate = filter.assignedFrom()
                    .atStartOfDay(ZoneId.of("Africa/Lagos"))
                    .toInstant();

            sql.append(" AND assigned_at >= ? ");
            params.add(Timestamp.from(startDate));
        }

        if (filter.assignedTo() != null) {
            Instant endDate = filter.assignedTo()
                    .plusDays(1)
                    .atStartOfDay(ZoneId.of("Africa/Lagos"))
                    .toInstant();

            sql.append(" AND assigned_at < ? ");
            params.add(Timestamp.from(endDate));
        }

        if (filter.title() != null && !filter.title().isBlank()) {

            String title = filter.title().trim().toLowerCase();

            String pattern = switch (filter.titleMatch() == null
                    ? com.aasismgt.enums.TitleMatch.CONTAINS
                    : filter.titleMatch()) {

                case STARTS_WITH -> title + "%";
                case ENDS_WITH -> "%" + title;
                case CONTAINS -> "%" + title + "%";
            };

            sql.append(" AND LOWER(title) LIKE ?");
            params.add(pattern);
        }
        return params;
    }
}
