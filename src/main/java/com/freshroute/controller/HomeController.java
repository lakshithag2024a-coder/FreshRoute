package com.freshroute.controller;

import com.freshroute.dto.ImpactMetricsDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.service.FoodListingService;
import com.freshroute.service.ImpactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final FoodListingService foodListingService;
    private final ImpactService impactService;

    public HomeController(FoodListingService foodListingService, ImpactService impactService) {
        this.foodListingService = foodListingService;
        this.impactService = impactService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<FoodListing> featuredListings = foodListingService.getFeaturedAvailableListings();
        ImpactMetricsDto impact = impactService.getPlatformImpactMetrics();

        model.addAttribute("featuredListings", featuredListings);
        model.addAttribute("impact", impact);
        return "index";
    }

    @GetMapping("/impact")
    public String impact(Model model) {
        ImpactMetricsDto impact = impactService.getPlatformImpactMetrics();
        model.addAttribute("impact", impact);
        return "impact";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
