package com.freshroute.service;

import com.freshroute.entity.FoodListing;
import com.freshroute.entity.Reservation;
import com.freshroute.entity.User;
import com.freshroute.enums.*;
import com.freshroute.repository.FoodListingRepository;
import com.freshroute.repository.ReservationRepository;
import com.freshroute.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FoodListingRepository foodListingRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${freshroute.seed.demo-data:true}")
    private boolean seedDemoData;

    public DemoDataInitializer(UserRepository userRepository,
                               FoodListingRepository foodListingRepository,
                               ReservationRepository reservationRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.foodListingRepository = foodListingRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Controlled initialization: never overwrite existing data
        if (!seedDemoData || userRepository.count() > 0) {
            return;
        }

        System.out.println(">>> Initializing FreshRoute Realistic Demonstration Data...");

        // 1. Create Administrator
        User admin = new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "Alexander Wright",
                "admin@freshroute.org",
                "FreshRoute Central Coordination",
                Role.ROLE_ADMIN,
                "+1 (555) 019-2831",
                "100 Green Boulevard, Suite 400",
                "San Francisco",
                "94103"
        );
        userRepository.save(admin);

        // 2. Create Donors
        User donorBistro = new User(
                "greenleaf",
                passwordEncoder.encode("password123"),
                "Elena Vance",
                "elena@greenleafbistro.com",
                "Green Leaf Bistro",
                Role.ROLE_DONOR,
                "+1 (555) 234-5678",
                "104 Grove St",
                "San Francisco",
                "94102"
        );
        donorBistro.setLatitude(37.7785);
        donorBistro.setLongitude(-122.4208);
        userRepository.save(donorBistro);

        User donorBakery = new User(
                "downtownbakery",
                passwordEncoder.encode("password123"),
                "Marco Rossi",
                "marco@downtownartisan.com",
                "Downtown Artisan Bakery",
                Role.ROLE_DONOR,
                "+1 (555) 345-6789",
                "450 Sutter St",
                "San Francisco",
                "94108"
        );
        donorBakery.setLatitude(37.7895);
        donorBakery.setLongitude(-122.4082);
        userRepository.save(donorBakery);

        User donorMarket = new User(
                "citysupermarket",
                passwordEncoder.encode("password123"),
                "Jessica Chen",
                "jessica@cityfreshmarket.com",
                "City Fresh Supermarket",
                Role.ROLE_DONOR,
                "+1 (555) 456-7890",
                "880 Folsom St",
                "San Francisco",
                "94107"
        );
        donorMarket.setLatitude(37.7801);
        donorMarket.setLongitude(-122.4045);
        userRepository.save(donorMarket);

        User donorMess = new User(
                "campusmess",
                passwordEncoder.encode("password123"),
                "Chef Robert Taylor",
                "dining@techcampus.edu",
                "Tech University Dining Hall",
                Role.ROLE_DONOR,
                "+1 (555) 567-8901",
                "500 Parnassus Ave",
                "San Francisco",
                "94143"
        );
        donorMess.setLatitude(37.7631);
        donorMess.setLongitude(-122.4578);
        userRepository.save(donorMess);

        // 3. Create Recipients
        User recipientShelter = new User(
                "hopeshelter",
                passwordEncoder.encode("password123"),
                "Pastor Thomas Evans",
                "contact@hopecommunityshelter.org",
                "Hope Community Shelter",
                Role.ROLE_RECIPIENT,
                "+1 (555) 678-9012",
                "220 Golden Gate Ave",
                "San Francisco",
                "94102"
        );
        userRepository.save(recipientShelter);

        User recipientYouth = new User(
                "sunshineyouth",
                passwordEncoder.encode("password123"),
                "Maria Rodriguez",
                "maria@sunshineyouth.org",
                "Sunshine Youth Outreach",
                Role.ROLE_RECIPIENT,
                "+1 (555) 789-0123",
                "730 Polk St",
                "San Francisco",
                "94109"
        );
        userRepository.save(recipientYouth);

        User recipientNgo = new User(
                "foodforall",
                passwordEncoder.encode("password123"),
                "Kenji Sato",
                "kenji@foodforallvolunteers.org",
                "Food For All Volunteers",
                Role.ROLE_RECIPIENT,
                "+1 (555) 890-1234",
                "312 9th St",
                "San Francisco",
                "94103"
        );
        userRepository.save(recipientNgo);

        User recipientMaya = new User(
                "volunteer_maya",
                passwordEncoder.encode("password123"),
                "Maya Patel",
                "maya.patel@communityhelper.org",
                "Mission District Community Pantry",
                Role.ROLE_RECIPIENT,
                "+1 (555) 901-2345",
                "1540 Mission St",
                "San Francisco",
                "94103"
        );
        userRepository.save(recipientMaya);

        LocalDateTime now = LocalDateTime.now();

        // 4. Create Diverse Food Listings
        // Listing 1: Cooked Meals - URGENT (expires in 1.5 hours)
        FoodListing list1 = new FoodListing();
        list1.setDonor(donorBistro);
        list1.setTitle("Mediterranean Roasted Chicken & Lemon Herb Rice");
        list1.setCategory(FoodCategory.COOKED_MEALS);
        list1.setDescription("Freshly cooked lunch service surplus. High quality roasted chicken breast with saffron rice, grilled zucchini, and tzatziki sauce. Packaged in sanitized foil containers.");
        list1.setTotalQuantity(18);
        list1.setAvailableQuantity(18);
        list1.setUnit(FoodUnit.PORTIONS);
        list1.setPreparationTime(now.minusHours(3));
        list1.setPickupDeadline(now.plusMinutes(90)); // Urgent!
        list1.setPickupAddress("104 Grove St, Kitchen Back Entrance");
        list1.setDonorContact("+1 (555) 234-5678");
        list1.setAllergens("Contains dairy (tzatziki). Gluten-free.");
        list1.setStorageRequirement("Hot-holding until pickup or immediate refrigeration.");
        list1.setSafetyDeclarationAccepted(true);
        list1.setStatus(ListingStatus.AVAILABLE);
        list1.setSimulatedDistanceKm(1.4);
        foodListingRepository.save(list1);

        // Listing 2: Bakery Items - Same Day Evening (expires in 5 hours)
        FoodListing list2 = new FoodListing();
        list2.setDonor(donorBakery);
        list2.setTitle("Artisan Sourdough Loaves & Croissants");
        list2.setCategory(FoodCategory.BAKERY);
        list2.setDescription("Today's morning bake surplus: 8 country sourdough batards, 10 butter croissants, and 6 cinnamon brioche twists. Fresh, crusty, and perfect for dinner services.");
        list2.setTotalQuantity(24);
        list2.setAvailableQuantity(16); // 8 already reserved
        list2.setUnit(FoodUnit.ITEMS);
        list2.setPreparationTime(now.minusHours(6));
        list2.setPickupDeadline(now.plusHours(5));
        list2.setPickupAddress("450 Sutter St, Front Counter / Back Alley");
        list2.setDonorContact("+1 (555) 345-6789");
        list2.setAllergens("Contains gluten, butter, eggs.");
        list2.setStorageRequirement("Room temperature dry storage.");
        list2.setSafetyDeclarationAccepted(true);
        list2.setStatus(ListingStatus.AVAILABLE);
        list2.setSimulatedDistanceKm(2.8);
        foodListingRepository.save(list2);

        // Listing 3: Cooked Meals - College Mess (expires in 4 hours)
        FoodListing list3 = new FoodListing();
        list3.setDonor(donorMess);
        list3.setTitle("Vegetable Lasagna & Garlic Herb Bread");
        list3.setCategory(FoodCategory.COOKED_MEALS);
        list3.setDescription("Delicious multi-layer vegetarian lasagna with ricotta, mozzarella, spinach, and marinara sauce. Includes two trays of garlic baguettes from dining hall lunch.");
        list3.setTotalQuantity(25);
        list3.setAvailableQuantity(25);
        list3.setUnit(FoodUnit.PORTIONS);
        list3.setPreparationTime(now.minusHours(2));
        list3.setPickupDeadline(now.plusHours(4));
        list3.setPickupAddress("500 Parnassus Ave, Loading Bay D");
        list3.setDonorContact("+1 (555) 567-8901");
        list3.setAllergens("Contains wheat, milk, cheese. Vegetarian.");
        list3.setStorageRequirement("Refrigerate at or below 4°C.");
        list3.setSafetyDeclarationAccepted(true);
        list3.setStatus(ListingStatus.AVAILABLE);
        list3.setSimulatedDistanceKm(4.2);
        foodListingRepository.save(list3);

        // Listing 4: Fruits and Vegetables - Supermarket (expires tomorrow)
        FoodListing list4 = new FoodListing();
        list4.setDonor(donorMarket);
        list4.setTitle("Crisp Gala Apples & Organic Carrots Crates");
        list4.setCategory(FoodCategory.FRUITS_AND_VEGETABLES);
        list4.setDescription("Surplus wholesale crates from morning inventory restock. High grade fresh apples and sweet washed bunch carrots in wooden crates.");
        list4.setTotalQuantity(12);
        list4.setAvailableQuantity(12);
        list4.setUnit(FoodUnit.BOXES);
        list4.setPreparationTime(now.minusHours(8));
        list4.setPickupDeadline(now.plusHours(24));
        list4.setPickupAddress("880 Folsom St, Customer Service Desk");
        list4.setDonorContact("+1 (555) 456-7890");
        list4.setAllergens("None. 100% Raw fresh produce.");
        list4.setStorageRequirement("Cool produce room / ventilated area.");
        list4.setSafetyDeclarationAccepted(true);
        list4.setStatus(ListingStatus.AVAILABLE);
        list4.setSimulatedDistanceKm(2.1);
        foodListingRepository.save(list4);

        // Listing 5: Dairy and Beverages (expires in 12 hours)
        FoodListing list5 = new FoodListing();
        list5.setDonor(donorMarket);
        list5.setTitle("Organic Whole Milk & Low-Fat Greek Yogurts");
        list5.setCategory(FoodCategory.DAIRY_AND_BEVERAGES);
        list5.setDescription("Cartons of fresh organic whole milk (1 gallon each) and sealed 500g pots of plain Greek yogurt nearing sell-by date but completely fresh and sealed.");
        list5.setTotalQuantity(15);
        list5.setAvailableQuantity(15);
        list5.setUnit(FoodUnit.ITEMS);
        list5.setPreparationTime(now.minusHours(4));
        list5.setPickupDeadline(now.plusHours(12));
        list5.setPickupAddress("880 Folsom St, Dairy Receiving Gate");
        list5.setDonorContact("+1 (555) 456-7890");
        list5.setAllergens("Contains milk / dairy.");
        list5.setStorageRequirement("Must be transported and stored below 4°C.");
        list5.setSafetyDeclarationAccepted(true);
        list5.setStatus(ListingStatus.AVAILABLE);
        list5.setSimulatedDistanceKm(2.1);
        foodListingRepository.save(list5);

        // Listing 6: Packaged Food (expires in 3 days)
        FoodListing list6 = new FoodListing();
        list6.setDonor(donorMarket);
        list6.setTitle("Whole Grain Granola & Canned Chickpea Pantry Pack");
        list6.setCategory(FoodCategory.PACKAGED_FOOD);
        list6.setDescription("Overstocked non-perishable pantry essentials: artisan toasted rolled oat granola packs and shelf-stable canned chickpeas/lentils.");
        list6.setTotalQuantity(30);
        list6.setAvailableQuantity(30);
        list6.setUnit(FoodUnit.ITEMS);
        list6.setPreparationTime(now.minusDays(1));
        list6.setPickupDeadline(now.plusDays(3));
        list6.setPickupAddress("880 Folsom St, Loading Dock 2");
        list6.setDonorContact("+1 (555) 456-7890");
        list6.setAllergens("Contains oats, may contain traces of tree nuts.");
        list6.setStorageRequirement("Ambient dry storage.");
        list6.setSafetyDeclarationAccepted(true);
        list6.setStatus(ListingStatus.AVAILABLE);
        list6.setSimulatedDistanceKm(2.1);
        foodListingRepository.save(list6);

        // Listing 7: Cooked Meals - Previously completed / COLLECTED
        FoodListing list7 = new FoodListing();
        list7.setDonor(donorBistro);
        list7.setTitle("Gourmet Lentil Stew with Steamed Jasmine Rice");
        list7.setCategory(FoodCategory.COOKED_MEALS);
        list7.setDescription("Hearty vegetable lentil stew prepared with cumin, carrots, and sweet potatoes. Successfully rescued yesterday.");
        list7.setTotalQuantity(20);
        list7.setAvailableQuantity(0);
        list7.setUnit(FoodUnit.PORTIONS);
        list7.setPreparationTime(now.minusDays(2));
        list7.setPickupDeadline(now.minusDays(1));
        list7.setPickupAddress("104 Grove St, Kitchen Side");
        list7.setDonorContact("+1 (555) 234-5678");
        list7.setAllergens("Vegan, Gluten-free.");
        list7.setStorageRequirement("Refrigerated.");
        list7.setSafetyDeclarationAccepted(true);
        list7.setStatus(ListingStatus.COLLECTED);
        list7.setSimulatedDistanceKm(1.4);
        foodListingRepository.save(list7);

        // Listing 8: Expired listing for Expiry Management demonstration
        FoodListing list8 = new FoodListing();
        list8.setDonor(donorMess);
        list8.setTitle("Chilled Pasta Salad with Cherry Tomatoes");
        list8.setCategory(FoodCategory.COOKED_MEALS);
        list8.setDescription("Surplus cold pasta salad from previous catering event that reached pickup deadline uncollected.");
        list8.setTotalQuantity(10);
        list8.setAvailableQuantity(10);
        list8.setUnit(FoodUnit.PORTIONS);
        list8.setPreparationTime(now.minusDays(1));
        list8.setPickupDeadline(now.minusHours(3)); // Expired in the past!
        list8.setPickupAddress("500 Parnassus Ave, Hall B");
        list8.setDonorContact("+1 (555) 567-8901");
        list8.setAllergens("Gluten, dairy.");
        list8.setStorageRequirement("Refrigerated.");
        list8.setSafetyDeclarationAccepted(true);
        list8.setStatus(ListingStatus.EXPIRED);
        list8.setSimulatedDistanceKm(4.2);
        foodListingRepository.save(list8);

        // 5. Create Sample Reservations in Different States
        // Reservation A: On list2 (Bakery) - CONFIRMED status
        Reservation res1 = new Reservation();
        res1.setReservationCode("FR-2026-B8K2");
        res1.setListing(list2);
        res1.setRecipient(recipientShelter);
        res1.setRequestedQuantity(8);
        res1.setStatus(ReservationStatus.CONFIRMED);
        res1.setPickupNotes("Van volunteer will arrive at 4:30 PM with sanitized transport boxes.");
        res1.setReservedAt(now.minusHours(2));
        res1.setConfirmedAt(now.minusHours(1));
        reservationRepository.save(res1);

        // Reservation B: On list7 (Bistro) - COLLECTED status
        Reservation res2 = new Reservation();
        res2.setReservationCode("FR-2026-M4P9");
        res2.setListing(list7);
        res2.setRecipient(recipientYouth);
        res2.setRequestedQuantity(20);
        res2.setStatus(ReservationStatus.COLLECTED);
        res2.setPickupNotes("Collected for evening youth shelter community meal.");
        res2.setReservedAt(now.minusDays(2).plusHours(1));
        res2.setConfirmedAt(now.minusDays(2).plusHours(2));
        res2.setCollectedAt(now.minusDays(1).minusHours(2));
        reservationRepository.save(res2);

        // Reservation C: Sample Cancelled Reservation
        Reservation res3 = new Reservation();
        res3.setReservationCode("FR-2026-C1X7");
        res3.setListing(list4);
        res3.setRecipient(recipientNgo);
        res3.setRequestedQuantity(4);
        res3.setStatus(ReservationStatus.CANCELLED);
        res3.setPickupNotes("Could not arrange vehicle in time.");
        res3.setReservedAt(now.minusDays(1));
        res3.setCancelledAt(now.minusHours(12));
        res3.setCancellationReason("Driver unavailable for morning route");
        reservationRepository.save(res3);

        System.out.println(">>> Demo data successfully seeded: 9 users, 8 listings, 3 sample reservations.");
    }
}
