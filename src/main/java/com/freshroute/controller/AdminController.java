package com.freshroute.controller;

import com.freshroute.dto.ImpactMetricsDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.Role;
import com.freshroute.repository.UserRepository;
import com.freshroute.service.FoodListingService;
import com.freshroute.service.ImpactService;
import com.freshroute.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final FoodListingService foodListingService;
    private final ReservationService reservationService;
    private final ImpactService impactService;

    public AdminController(UserRepository userRepository,
                           FoodListingService foodListingService,
                           ReservationService reservationService,
                           ImpactService impactService) {
        this.userRepository = userRepository;
        this.foodListingService = foodListingService;
        this.reservationService = reservationService;
        this.impactService = impactService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        List<FoodListing> listings = foodListingService.getAllListings();
        List<Reservation> reservations = reservationService.getAllReservations();
        ImpactMetricsDto impact = impactService.getPlatformImpactMetrics();

        long donorCount = users.stream().filter(u -> u.getRole() == Role.ROLE_DONOR).count();
        long recipientCount = users.stream().filter(u -> u.getRole() == Role.ROLE_RECIPIENT).count();

        model.addAttribute("users", users);
        model.addAttribute("listings", listings);
        model.addAttribute("reservations", reservations);
        model.addAttribute("impact", impact);
        model.addAttribute("donorCount", donorCount);
        model.addAttribute("recipientCount", recipientCount);
        model.addAttribute("totalUsers", users.size());

        return "admin/dashboard";
    }
}
