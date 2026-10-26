package com.intellidesk.repository;

import com.intellidesk.entity.Category;
import com.intellidesk.entity.Priority;
import com.intellidesk.entity.Ticket;
import com.intellidesk.entity.TicketStatus;
import com.intellidesk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByCreatedBy(User user, Pageable pageable);

    Page<Ticket> findByAssignedTo(User user, Pageable pageable);

    List<Ticket> findTop5ByCreatedByOrderByCreatedAtDesc(User user);

    List<Ticket> findTop5ByOrderByCreatedAtDesc();

    long countByStatus(TicketStatus status);

    long countByPriority(Priority priority);

    long countByCategory(Category category);

    long countByCreatedBy(User user);

    long countByCreatedByAndStatus(User user, TicketStatus status);

    long countByAssignedTo(User user);

    long countByAssignedToAndStatus(User user, TicketStatus status);

    long countByAssignedToAndPriority(User user, Priority priority);

    @Query("SELECT MAX(t.id) FROM Ticket t")
    Long findMaxId();

    @Query("SELECT c.name, COUNT(t) FROM Ticket t JOIN t.category c GROUP BY c.name")
    List<Object[]> countTicketsByCategory();

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();

    @Query("SELECT FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') as month, COUNT(t) " +
           "FROM Ticket t " +
           "GROUP BY FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') " +
           "ORDER BY month ASC")
    List<Object[]> countTicketsByMonth();
}
