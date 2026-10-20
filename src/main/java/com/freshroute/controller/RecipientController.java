package com.freshroute.controller;

import com.freshroute.dto.MatchingCriteriaDto;
import com.freshroute.dto.SmartMatchResultDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.FoodCategory;
import com.freshroute.enums.ReservationStatus;
import com.freshroute.service.FoodListingService;
import com.freshroute.service.ReservationService;
import com.freshroute.service.SmartMatchingService;
import com.freshroute.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/recipient")
public class RecipientController {

    private final ReservationService reservationService;
    private final SmartMatchingService smartMatchingService;
    private final FoodListingService foodListingService;
    private final UserService userService;

    public RecipientController(ReservationService reservationService,
                               SmartMatchingService smartMatchingService,
                               FoodListingService foodListingService,
                               UserService userService) {
        this.reservationService = reservationService;
        this.smartMatchingService = smartMatchingService;
        this.foodListingService = foodListingService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User recipient = userService.getCurrentAuthenticatedUser();
        List<Reservation> reservations = reservationService.getReservationsByRecipient(recipient);
        List<FoodListing> featuredSurplus = foodListingService.getFeaturedAvailableListings();

        long activeReservationsCount = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.CONFIRMED)
                .count();
        long completedPickupsCount = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COLLECTED)
                .count();
        long totalMealsRescued = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COLLECTED)
                .mapToLong(Reservation::getRequestedQuantity).sum();

        model.addAttribute("recipient", recipient);
        model.addAttribute("reservations", reservations);
        model.addAttribute("activeReservations", reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.CONFIRMED)
                .toList());
        model.addAttribute("featuredSurplus", featuredSurplus);
        model.addAttribute("activeReservationsCount", activeReservationsCount);
        model.addAttribute("completedPickupsCount", completedPickupsCount);
        model.addAttribute("totalMealsRescued", totalMealsRescued);

        return "recipient/dashboard";
    }

    @GetMapping("/smart-matches")
    public String smartMatches(@ModelAttribute("criteria") MatchingCriteriaDto criteria, Model model) {
        if (criteria == null) {
            criteria = new MatchingCriteriaDto();
        }

        List<SmartMatchResultDto> matchResults = smartMatchingService.findMatches(criteria);

        model.addAttribute("criteria", criteria);
        model.addAttribute("matchResults", matchResults);
        model.addAttribute("categories", FoodCategory.values());
        model.addAttribute("urgencyWeight", (int)(smartMatchingService.getDefaultUrgencyWeight() * 100));
        model.addAttribute("quantityWeight", (int)(smartMatchingService.getDefaultQuantityWeight() * 100));
        model.addAttribute("distanceWeight", (int)(smartMatchingService.getDefaultDistanceWeight() * 100));

        return "recipient/smart-matches";
    }

    @GetMapping("/reservations")
    public String reservationHistory(@RequestParam(value = "status", required = false) ReservationStatus status, Model model) {
        User recipient = userService.getCurrentAuthenticatedUser();
        List<Reservation> all = reservationService.getReservationsByRecipient(recipient);

        List<Reservation> filtered = (status != null)
                ? all.stream().filter(r -> r.getStatus() == status).toList()
                : all;

        model.addAttribute("reservations", filtered);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("totalCount", all.size());

        return "recipient/reservations";
    }
}
