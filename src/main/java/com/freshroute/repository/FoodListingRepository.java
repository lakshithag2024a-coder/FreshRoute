package com.freshroute.repository;

import com.freshroute.entity.FoodListing;
import com.freshroute.entity.User;
import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    List<FoodListing> findByDonorOrderByCreatedAtDesc(User donor);

    List<FoodListing> findByDonorAndStatusOrderByCreatedAtDesc(User donor, ListingStatus status);

    List<FoodListing> findByStatusAndPickupDeadlineAfterAndAvailableQuantityGreaterThanOrderByPickupDeadlineAsc(
            ListingStatus status, LocalDateTime deadline, Integer minQty);

    @Query("SELECT f FROM FoodListing f WHERE f.status = 'AVAILABLE' AND f.availableQuantity > 0 AND f.pickupDeadline > :now " +
           "AND (:category IS NULL OR f.category = :category) " +
           "AND (:query IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(f.pickupAddress) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'urgency' THEN f.pickupDeadline END ASC, " +
           "CASE WHEN :sortBy = 'quantity' THEN f.availableQuantity END DESC, " +
           "CASE WHEN :sortBy = 'newest' THEN f.createdAt END DESC, " +
           "f.pickupDeadline ASC")
    List<FoodListing> searchMarketplace(@Param("now") LocalDateTime now,
                                       @Param("query") String query,
                                       @Param("category") FoodCategory category,
                                       @Param("sortBy") String sortBy);

    List<FoodListing> findByPickupDeadlineBeforeAndStatus(LocalDateTime deadline, ListingStatus status);

    long countByStatus(ListingStatus status);

    @Query("SELECT COUNT(f) FROM FoodListing f WHERE f.status = 'AVAILABLE' AND f.pickupDeadline > :now AND f.availableQuantity > 0")
    long countCurrentlyAvailable(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(f) FROM FoodListing f WHERE f.status = 'EXPIRED' OR (f.status = 'AVAILABLE' AND f.pickupDeadline <= :now)")
    long countExpiredOrUnclaimed(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(f.availableQuantity), 0) FROM FoodListing f WHERE f.status = 'AVAILABLE' AND f.pickupDeadline > :now")
    long sumAvailableQuantity(@Param("now") LocalDateTime now);

    @Query("SELECT f.category, COUNT(f), COALESCE(SUM(f.totalQuantity), 0) FROM FoodListing f GROUP BY f.category")
    List<Object[]> getListingStatsByCategory();

    @Query("SELECT f.status, COUNT(f) FROM FoodListing f GROUP BY f.status")
    List<Object[]> getListingCountByStatus();

    List<FoodListing> findTop6ByStatusAndPickupDeadlineAfterAndAvailableQuantityGreaterThanOrderByPickupDeadlineAsc(
            ListingStatus status, LocalDateTime deadline, Integer minQty);
}
