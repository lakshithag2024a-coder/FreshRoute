package com.freshroute.dto;

import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.FoodUnit;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class FoodListingDto {

    private Long id;

    @NotBlank(message = "Food name/title is required")
    private String title;

    @NotNull(message = "Please select a food category")
    private FoodCategory category;

    @NotBlank(message = "Please provide a detailed food description")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Available quantity must be at least 1")
    private Integer totalQuantity;

    @NotNull(message = "Please select a unit of measurement")
    private FoodUnit unit;

    @NotNull(message = "Preparation time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime preparationTime = LocalDateTime.now();

    @NotNull(message = "Pickup deadline is required")
    @Future(message = "Pickup deadline must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime pickupDeadline;

    @NotBlank(message = "Pickup location address is required")
    private String pickupAddress;

    private String donorContact;

    private String allergens;

    private String storageRequirement;

    @AssertTrue(message = "You must confirm the food safety declaration to publish this surplus listing")
    private Boolean safetyDeclarationAccepted = false;

    private Double simulatedDistanceKm = 3.2;

    public FoodListingDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

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

    public Double getSimulatedDistanceKm() { return simulatedDistanceKm; }
    public void setSimulatedDistanceKm(Double simulatedDistanceKm) { this.simulatedDistanceKm = simulatedDistanceKm; }
}
