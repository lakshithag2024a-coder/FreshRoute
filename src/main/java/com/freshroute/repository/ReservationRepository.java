package com.freshroute.repository;

import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRecipientOrderByReservedAtDesc(User recipient);

    List<Reservation> findByListingDonorOrderByReservedAtDesc(User donor);

    List<Reservation> findByListing(FoodListing listing);

    Optional<Reservation> findByReservationCode(String reservationCode);

    boolean existsByRecipientAndListingAndStatusIn(User recipient, FoodListing listing, List<ReservationStatus> statuses);

    long countByStatus(ReservationStatus status);

    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM Reservation r WHERE r.status = :status")
    long sumRequestedQuantityByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RESERVED' AND r.listing.pickupDeadline <= :now")
    List<Reservation> findPendingReservationsWithPassedDeadline(@Param("now") LocalDateTime now);

    @Query("SELECT r.listing.category, COALESCE(SUM(r.requestedQuantity), 0) FROM Reservation r WHERE r.status = 'COLLECTED' GROUP BY r.listing.category")
    List<Object[]> getCollectedQuantityByCategory();

    @Query("SELECT FUNCTION('FORMATDATETIME', r.collectedAt, 'yyyy-MM'), COUNT(r), COALESCE(SUM(r.requestedQuantity), 0) " +
           "FROM Reservation r WHERE r.status = 'COLLECTED' AND r.collectedAt IS NOT NULL " +
           "GROUP BY FUNCTION('FORMATDATETIME', r.collectedAt, 'yyyy-MM') " +
           "ORDER BY FUNCTION('FORMATDATETIME', r.collectedAt, 'yyyy-MM') ASC")
    List<Object[]> getPickupsOverTime();

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.listing.donor = :donor AND r.status = :status")
    long countByDonorAndStatus(@Param("donor") User donor, @Param("status") ReservationStatus status);

    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM Reservation r WHERE r.listing.donor = :donor AND r.status = 'COLLECTED'")
    long sumCollectedQuantityByDonor(@Param("donor") User donor);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.recipient = :recipient AND r.status = :status")
    long countByRecipientAndStatus(@Param("recipient") User recipient, @Param("status") ReservationStatus status);

    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM Reservation r WHERE r.recipient = :recipient AND r.status = 'COLLECTED'")
    long sumCollectedQuantityByRecipient(@Param("recipient") User recipient);
}
