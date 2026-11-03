package com.aasismgt.repository;

import com.aasismgt.entities.Ticket;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository  extends JpaRepository<Ticket, Long>{
//    List<Ticket> findByGeneralStatus(GeneralStatus generalStatus);

    Page<Ticket> findAllByAssignedToAndGeneralStatus(Long assignedTo, GeneralStatus generalStatus, Pageable pageable);

    Page<Ticket> findByGeneralStatusAndProgressStatus(
            GeneralStatus generalStatus, ProgressStatus progressStatus, Pageable pageable);

    Optional<Ticket> findByIdAndGeneralStatus(
            Long id, GeneralStatus generalStatus);
}
