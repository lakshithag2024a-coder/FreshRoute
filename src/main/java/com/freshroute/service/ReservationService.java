package com.freshroute.service;

import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.ListingStatus;
import com.freshroute.enums.ReservationStatus;
import com.freshroute.repository.FoodListingRepository;
import com.freshroute.repository.ReservationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final FoodListingRepository foodListingRepository;
    private final SecureRandom random = new SecureRandom();

    public ReservationService(ReservationRepository reservationRepository, FoodListingRepository foodListingRepository) {
        this.reservationRepository = reservationRepository;
        this.foodListingRepository = foodListingRepository;
    }

    /**
     * Recipient reserves surplus food.
     * Prevents overbooking, checks duplicate reservations, decreases available quantity.
     */
    @Transactional
    public synchronized Reservation createReservation(Long listingId, Integer requestedQuantity, String notes, User recipient) {
        FoodListing listing = foodListingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Food listing not found."));

        // Expiry check
        if (listing.isExpired() || listing.getStatus() == ListingStatus.EXPIRED) {
            throw new IllegalStateException("Cannot reserve food: The pickup deadline for this listing has expired.");
        }

        // Availability check
        if (listing.getStatus() != ListingStatus.AVAILABLE || listing.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("This food listing is currently not available for reservation.");
        }

        // Positive quantity check
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be at least 1.");
        }

        // Quantity limit check
        if (requestedQuantity > listing.getAvailableQuantity()) {
            throw new IllegalStateException("Requested quantity (" + requestedQuantity + 
                    ") exceeds available quantity (" + listing.getAvailableQuantity() + " " + listing.getUnit().getDisplayName() + ").");
        }

        // Duplicate active reservation check
        boolean hasActive = reservationRepository.existsByRecipientAndListingAndStatusIn(
                recipient, listing, Arrays.asList(ReservationStatus.RESERVED, ReservationStatus.CONFIRMED));
        if (hasActive) {
            throw new IllegalStateException("You already have an active reservation for this food listing. Please check your reservations.");
        }

        // Generate unique reservation code
        String reservationCode = generateReservationCode();

        Reservation reservation = new Reservation();
        reservation.setReservationCode(reservationCode);
        reservation.setListing(listing);
        reservation.setRecipient(recipient);
        reservation.setRequestedQuantity(requestedQuantity);
        reservation.setPickupNotes(notes != null ? notes.trim() : null);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setReservedAt(LocalDateTime.now());

        // Deduct available quantity from listing
        int remaining = listing.getAvailableQuantity() - requestedQuantity;
        listing.setAvailableQuantity(remaining);
        if (remaining == 0) {
            listing.setStatus(ListingStatus.RESERVED);
        }
        listing.setUpdatedAt(LocalDateTime.now());
        foodListingRepository.save(listing);

        return reservationRepository.save(reservation);
    }

    /**
     * Donor confirms the incoming reservation.
     */
    @Transactional
    public Reservation confirmReservation(Long reservationId, User donor) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + reservationId));

        // Donor verification
        if (!reservation.getListing().getDonor().getId().equals(donor.getId()) && !donor.isAdmin()) {
            throw new AccessDeniedException("You are not authorized to confirm this reservation.");
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Only reservations in 'RESERVED' status can be confirmed.");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    /**
     * Donor marks reservation as collected after physical handover.
     */
    @Transactional
    public Reservation markCollected(Long reservationId, User donor) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + reservationId));

        if (!reservation.getListing().getDonor().getId().equals(donor.getId()) && !donor.isAdmin()) {
            throw new AccessDeniedException("You are not authorized to complete this pickup.");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only 'CONFIRMED' reservations can be marked as collected.");
        }

        reservation.setStatus(ReservationStatus.COLLECTED);
        reservation.setCollectedAt(LocalDateTime.now());
        Reservation savedReservation = reservationRepository.save(reservation);

        // If listing has 0 available quantity and all reservations are collected/cancelled, mark listing COLLECTED
        FoodListing listing = reservation.getListing();
        if (listing.getAvailableQuantity() == 0) {
            listing.setStatus(ListingStatus.COLLECTED);
            listing.setUpdatedAt(LocalDateTime.now());
            foodListingRepository.save(listing);
        }

        return savedReservation;
    }

    /**
     * Cancel an active reservation (by Recipient or Donor).
     * Restores the reserved quantity back to the listing.
     */
    @Transactional
    public synchronized Reservation cancelReservation(Long reservationId, String reason, User currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + reservationId));

        boolean isRecipient = reservation.getRecipient().getId().equals(currentUser.getId());
        boolean isDonor = reservation.getListing().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.isAdmin();

        if (!isRecipient && !isDonor && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to cancel this reservation.");
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel reservation with status: " + reservation.getStatus().getDisplayName());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(reason != null && !reason.isBlank() ? reason.trim() : 
                (isRecipient ? "Cancelled by recipient" : "Cancelled by donor"));

        // Restore quantity to listing
        FoodListing listing = reservation.getListing();
        listing.setAvailableQuantity(listing.getAvailableQuantity() + reservation.getRequestedQuantity());

        // If listing was marked RESERVED, restore to AVAILABLE if deadline not passed
        if (listing.getStatus() == ListingStatus.RESERVED && !listing.isExpired()) {
            listing.setStatus(ListingStatus.AVAILABLE);
        }
        listing.setUpdatedAt(LocalDateTime.now());
        foodListingRepository.save(listing);

        return reservationRepository.save(reservation);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> findByReservationCode(String code) {
        return reservationRepository.findByReservationCode(code);
    }

    public List<Reservation> getReservationsByRecipient(User recipient) {
        return reservationRepository.findByRecipientOrderByReservedAtDesc(recipient);
    }

    public List<Reservation> getIncomingReservationsForDonor(User donor) {
        return reservationRepository.findByListingDonorOrderByReservedAtDesc(donor);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    private String generateReservationCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("FR-2026-");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
