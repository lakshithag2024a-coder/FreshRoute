package com.freshroute.controller;

import com.freshroute.dto.ReservationRequestDto;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.service.ReservationService;
import com.freshroute.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @PostMapping("/new")
    public String createReservation(@Valid @ModelAttribute("reservationDto") ReservationRequestDto dto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        User recipient = userService.getCurrentAuthenticatedUser();
        if (recipient == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid reservation request. Check requested quantity.");
            return "redirect:/marketplace/listings/" + dto.getListingId();
        }

        try {
            Reservation reservation = reservationService.createReservation(
                    dto.getListingId(), dto.getRequestedQuantity(), dto.getPickupNotes(), recipient);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Surplus food reserved successfully! Reservation code: " + reservation.getReservationCode());
            return "redirect:/reservations/" + reservation.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/marketplace/listings/" + dto.getListingId();
        }
    }

    @GetMapping("/{id}")
    public String viewReservationDetails(@PathVariable("id") Long id, Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        Reservation reservation = reservationService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + id));

        // Authorization: Recipient, Donor of the listing, or Admin
        boolean isRecipient = reservation.getRecipient().getId().equals(currentUser.getId());
        boolean isDonor = reservation.getListing().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isRecipient && !isDonor && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this reservation.");
        }

        model.addAttribute("reservation", reservation);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isRecipient", isRecipient);
        model.addAttribute("isDonor", isDonor);
        model.addAttribute("isAdmin", isAdmin);

        return "reservations/details";
    }

    @PostMapping("/{id}/confirm")
    public String confirmReservation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        try {
            reservationService.confirmReservation(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation confirmed! The recipient has been notified to pick up the food.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reservations/" + id;
    }

    @PostMapping("/{id}/collect")
    public String markCollected(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        try {
            reservationService.markCollected(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Pickup completed! Food rescued successfully and recorded in your impact tally.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reservations/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable("id") Long id,
                                    @RequestParam(value = "reason", required = false) String reason,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        try {
            reservationService.cancelReservation(id, reason, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled. Reserved food quantity has been restored to the marketplace.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reservations/" + id;
    }
}
