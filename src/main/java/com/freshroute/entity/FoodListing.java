package com.freshroute.entity;

import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.FoodUnit;
import com.freshroute.enums.ListingStatus;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_listings")
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoodCategory category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FoodUnit unit;

    @Column(nullable = false)
    private LocalDateTime preparationTime;

    @Column(nullable = false)
    private LocalDateTime pickupDeadline;

    @Column(nullable = false, length = 255)
    private String pickupAddress;

    @Column(length = 50)
    private String donorContact;

    @Column(length = 200)
    private String allergens;

    @Column(length = 100)
    private String storageRequirement;

    @Column(nullable = false)
    private Boolean safetyDeclarationAccepted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.AVAILABLE;

    @Column(nullable = false)
    private Double simulatedDistanceKm = 3.5;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public FoodListing() {}

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(pickupDeadline);
    }

    public boolean isAvailableForReservation() {
        return status == ListingStatus.AVAILABLE && availableQuantity > 0 && !isExpired();
    }

    public long getHoursRemaining() {
        if (isExpired()) return 0;
        return Duration.between(LocalDateTime.now(), pickupDeadline).toHours();
    }

    public long getMinutesRemaining() {
        if (isExpired()) return 0;
        return Duration.between(LocalDateTime.now(), pickupDeadline).toMinutes();
    }

    public String getTimeRemainingDisplay() {
        if (isExpired()) {
            return "Expired";
        }
        Duration d = Duration.between(LocalDateTime.now(), pickupDeadline);
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        if (hours > 24) {
            long days = hours / 24;
            return days + " day" + (days > 1 ? "s" : "") + " left";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m left";
        } else {
            return minutes + "m left (Urgent!)";
        }
    }

    public String getUrgencyBadgeClass() {
        if (isExpired()) {
            return "bg-secondary text-white";
        }
        long minutes = getMinutesRemaining();
        if (minutes <= 120) { // Under 2 hours
            return "bg-danger text-white animate-pulse";
        } else if (minutes <= 360) { // Under 6 hours
            return "bg-warning text-dark";
        } else {
            return "bg-success text-white";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public FoodUnit getUnit() { return unit; }
    public void setUnit(FoodUnit unit) { this.unit = unit; }

    public LocalDateTime getPreparationTime() { return preparationTime; }
    public void setPreparationTime(LocalDateTime preparationTime) { this.preparationTime = preparationTime; }

    public LocalDateTime getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(LocalDateTime pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDonorContact() { return donorContact; }
    public void setDonorContact(String donorContact) { this.donorContact = donorContact; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public String getStorageRequirement() { return storageRequirement; }
    public void setStorageRequirement(String storageRequirement) { this.storageRequirement = storageRequirement; }

    public Boolean getSafetyDeclarationAccepted() { return safetyDeclarationAccepted; }
    public void setSafetyDeclarationAccepted(Boolean safetyDeclarationAccepted) { this.safetyDeclarationAccepted = safetyDeclarationAccepted; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Double getSimulatedDistanceKm() { return simulatedDistanceKm; }
    public void setSimulatedDistanceKm(Double simulatedDistanceKm) { this.simulatedDistanceKm = simulatedDistanceKm; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }
}
