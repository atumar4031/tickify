package com.aasismgt.repository;

import com.aasismgt.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository  extends JpaRepository<Ticket, Long>{
}
