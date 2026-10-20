package com.freshroute.controller;

import com.freshroute.dto.ReservationRequestDto;
import com.freshroute.entity.FoodListing;
import com.freshroute.entity.User;
import com.freshroute.enums.FoodCategory;
import com.freshroute.service.FoodListingService;
import com.freshroute.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MarketplaceController {

    private final FoodListingService foodListingService;
    private final UserService userService;

    public MarketplaceController(FoodListingService foodListingService, UserService userService) {
        this.foodListingService = foodListingService;
        this.userService = userService;
    }

    @GetMapping("/marketplace")
    public String browseMarketplace(@RequestParam(value = "q", required = false) String query,
                                   @RequestParam(value = "category", required = false) FoodCategory category,
                                   @RequestParam(value = "sort", defaultValue = "urgency") String sort,
                                   Model model) {
        List<FoodListing> listings = foodListingService.getAvailableMarketplaceListings(query, category, sort);

        model.addAttribute("listings", listings);
        model.addAttribute("query", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("categories", FoodCategory.values());
        model.addAttribute("totalCount", listings.size());

        return "marketplace/browse";
    }

    @GetMapping("/marketplace/listings/{id}")
    public String listingDetails(@PathVariable("id") Long id, Model model) {
        FoodListing listing = foodListingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food listing not found with ID: " + id));

        User currentUser = userService.getCurrentAuthenticatedUser();

        model.addAttribute("listing", listing);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("reservationDto", new ReservationRequestDto(listing.getId(), 1));

        return "marketplace/details";
    }
}
