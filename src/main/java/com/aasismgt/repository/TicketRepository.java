package com.aasismgt.repository;

import com.aasismgt.entities.Ticket;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository  extends JpaRepository<Ticket, Long>{
    List<Ticket> findByGeneralStatus(GeneralStatus generalStatus);

    List<Ticket> findByGeneralStatusAndAssignedTo(
            GeneralStatus generalStatus, Long assignedTo);

    List<Ticket> findByGeneralStatusAndProgressStatus(
            GeneralStatus generalStatus, ProgressStatus progressStatus);

    Optional<Ticket> findByIdAndGeneralStatus(
            Long id, GeneralStatus generalStatus);
}
