package com.freshroute.controller;

import com.freshroute.dto.FoodListingDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.FoodUnit;
import com.freshroute.enums.ListingStatus;
import com.freshroute.enums.ReservationStatus;
import com.freshroute.service.FoodListingService;
import com.freshroute.service.ReservationService;
import com.freshroute.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/donor")
public class DonorController {

    private final FoodListingService foodListingService;
    private final ReservationService reservationService;
    private final UserService userService;

    public DonorController(FoodListingService foodListingService,
                           ReservationService reservationService,
                           UserService userService) {
        this.foodListingService = foodListingService;
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        List<FoodListing> listings = foodListingService.getListingsByDonor(donor);
        List<Reservation> incomingReservations = reservationService.getIncomingReservationsForDonor(donor);

        long activeListingsCount = listings.stream().filter(l -> l.getStatus() == ListingStatus.AVAILABLE).count();
        long pendingReservationsCount = incomingReservations.stream().filter(r -> r.getStatus() == ReservationStatus.RESERVED).count();
        long completedPickupsCount = incomingReservations.stream().filter(r -> r.getStatus() == ReservationStatus.COLLECTED).count();
        long totalMealsRescued = incomingReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COLLECTED)
                .mapToLong(Reservation::getRequestedQuantity).sum();

        model.addAttribute("donor", donor);
        model.addAttribute("listings", listings);
        model.addAttribute("recentReservations", incomingReservations.stream().limit(5).toList());
        model.addAttribute("activeListingsCount", activeListingsCount);
        model.addAttribute("pendingReservationsCount", pendingReservationsCount);
        model.addAttribute("completedPickupsCount", completedPickupsCount);
        model.addAttribute("totalMealsRescued", totalMealsRescued);

        return "donor/dashboard";
    }

    @GetMapping("/listings")
    public String manageListings(@RequestParam(value = "status", required = false) ListingStatus status, Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        List<FoodListing> listings = (status != null)
                ? foodListingService.getListingsByDonorAndStatus(donor, status)
                : foodListingService.getListingsByDonor(donor);

        model.addAttribute("listings", listings);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ListingStatus.values());
        return "donor/listings";
    }

    @GetMapping("/listings/new")
    public String showCreateForm(Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        FoodListingDto dto = new FoodListingDto();
        dto.setPickupAddress(donor.getAddress() != null ? donor.getAddress() + ", " + donor.getCity() : "");
        dto.setDonorContact(donor.getPhone() != null ? donor.getPhone() : "");
        dto.setPickupDeadline(LocalDateTime.now().plusHours(6));

        model.addAttribute("listingDto", dto);
        model.addAttribute("categories", FoodCategory.values());
        model.addAttribute("units", FoodUnit.values());
        return "donor/create-listing";
    }

    @PostMapping("/listings/new")
    public String createListing(@Valid @ModelAttribute("listingDto") FoodListingDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User donor = userService.getCurrentAuthenticatedUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", FoodCategory.values());
            model.addAttribute("units", FoodUnit.values());
            return "donor/create-listing";
        }

        try {
            FoodListing created = foodListingService.createListing(dto, donor);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Surplus food listing '" + created.getTitle() + "' published successfully!");
            return "redirect:/donor/listings";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", FoodCategory.values());
            model.addAttribute("units", FoodUnit.values());
            return "donor/create-listing";
        }
    }

    @GetMapping("/listings/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        FoodListing listing = foodListingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (!listing.getDonor().getId().equals(donor.getId()) && !donor.isAdmin()) {
            return "redirect:/access-denied";
        }

        FoodListingDto dto = new FoodListingDto();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setCategory(listing.getCategory());
        dto.setDescription(listing.getDescription());
        dto.setTotalQuantity(listing.getTotalQuantity());
        dto.setUnit(listing.getUnit());
        dto.setPreparationTime(listing.getPreparationTime());
        dto.setPickupDeadline(listing.getPickupDeadline());
        dto.setPickupAddress(listing.getPickupAddress());
        dto.setDonorContact(listing.getDonorContact());
        dto.setAllergens(listing.getAllergens());
        dto.setStorageRequirement(listing.getStorageRequirement());
        dto.setSafetyDeclarationAccepted(listing.getSafetyDeclarationAccepted());
        dto.setSimulatedDistanceKm(listing.getSimulatedDistanceKm());

        model.addAttribute("listingDto", dto);
        model.addAttribute("listing", listing);
        model.addAttribute("categories", FoodCategory.values());
        model.addAttribute("units", FoodUnit.values());
        return "donor/edit-listing";
    }

    @PostMapping("/listings/{id}/edit")
    public String updateListing(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("listingDto") FoodListingDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User donor = userService.getCurrentAuthenticatedUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", FoodCategory.values());
            model.addAttribute("units", FoodUnit.values());
            return "donor/edit-listing";
        }

        try {
            foodListingService.updateListing(id, dto, donor);
            redirectAttributes.addFlashAttribute("successMessage", "Listing updated successfully!");
            return "redirect:/donor/listings";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", FoodCategory.values());
            model.addAttribute("units", FoodUnit.values());
            return "donor/edit-listing";
        }
    }

    @PostMapping("/listings/{id}/delete")
    public String deleteListing(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User donor = userService.getCurrentAuthenticatedUser();
        try {
            foodListingService.deleteListing(id, donor);
            redirectAttributes.addFlashAttribute("successMessage", "Listing deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/donor/listings";
    }

    @GetMapping("/pickups")
    public String managePickups(Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        List<Reservation> reservations = reservationService.getIncomingReservationsForDonor(donor);

        model.addAttribute("reservations", reservations);
        model.addAttribute("pendingCount", reservations.stream().filter(r -> r.getStatus() == ReservationStatus.RESERVED).count());
        model.addAttribute("confirmedCount", reservations.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count());
        model.addAttribute("collectedCount", reservations.stream().filter(r -> r.getStatus() == ReservationStatus.COLLECTED).count());

        return "donor/pickups";
    }

    @GetMapping("/expiry-management")
    public String expiryManagement(Model model) {
        User donor = userService.getCurrentAuthenticatedUser();
        foodListingService.checkAndExpireListings(); // Refresh statuses

        List<FoodListing> allListings = foodListingService.getListingsByDonor(donor);
        List<FoodListing> urgentListings = allListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.AVAILABLE && l.getMinutesRemaining() <= 360)
                .toList();
        List<FoodListing> expiredListings = allListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.EXPIRED || l.isExpired())
                .toList();

        model.addAttribute("urgentListings", urgentListings);
        model.addAttribute("expiredListings", expiredListings);
        model.addAttribute("allListings", allListings);

        return "donor/expiry-management";
    }

    @PostMapping("/expiry-management/sweep")
    public String runExpirySweep(RedirectAttributes redirectAttributes) {
        foodListingService.checkAndExpireListings();
        redirectAttributes.addFlashAttribute("successMessage", "Expiry check completed. All listings updated to current server time.");
        return "redirect:/donor/expiry-management";
    }
}
