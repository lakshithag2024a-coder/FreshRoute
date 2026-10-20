package com.freshroute.controller;

import com.freshroute.dto.UserProfileDto;
import com.freshroute.entity.User;
import com.freshroute.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        User user = userService.getCurrentAuthenticatedUser();
        UserProfileDto dto = new UserProfileDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setOrganizationName(user.getOrganizationName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setCity(user.getCity());
        dto.setZipCode(user.getZipCode());

        model.addAttribute("user", user);
        model.addAttribute("profileDto", dto);
        return "profile/view";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileDto") UserProfileDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = userService.getCurrentAuthenticatedUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "profile/view";
        }

        try {
            userService.updateProfile(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile details updated successfully!");
            return "redirect:/profile";
        } catch (Exception ex) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", ex.getMessage());
            return "profile/view";
        }
    }
}
