package com.aasismgt.repository;

import com.aasismgt.entities.Ticket;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TicketRepository  extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
//    List<Ticket> findByGeneralStatus(GeneralStatus generalStatus);

    Page<Ticket> findAllByGeneralStatus(GeneralStatus generalStatus, Pageable pageable);

    Page<Ticket> findAllByAssignedToAndGeneralStatus(Long assignedTo, GeneralStatus generalStatus, Pageable pageable);

    Page<Ticket> findAllByGeneralStatusAndProgressStatus(
            GeneralStatus generalStatus, ProgressStatus progressStatus, Pageable pageable);

    Optional<Ticket> findByIdAndGeneralStatus(
            Long id, GeneralStatus generalStatus);

    Page<Ticket> findByAssignedToAndGeneralStatusAndProgressStatus(
            Long assignedTo,
            GeneralStatus generalStatus,
            ProgressStatus progressStatus,
            Pageable pageable
    );
}
