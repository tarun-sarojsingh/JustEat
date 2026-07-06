package com.justeat.config;

import com.justeat.model.MenuItem;
import com.justeat.model.Restaurant;
import com.justeat.model.Role;
import com.justeat.model.User;
import com.justeat.repository.MenuItemRepository;
import com.justeat.repository.RestaurantRepository;
import com.justeat.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("docker")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RestaurantRepository restaurantRepository,
                           MenuItemRepository menuItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (restaurantRepository.count() > 0 || userRepository.count() > 0) {
            return;
        }

        User owner = new User(
                "owner",
                "owner@justeat.local",
                passwordEncoder.encode("Password123!"),
                Role.RESTAURANT_OWNER
        );
        owner = userRepository.save(owner);

        User customer = new User(
                "customer",
                "customer@justeat.local",
                passwordEncoder.encode("Password123!"),
                Role.CUSTOMER
        );
        userRepository.save(customer);

        Restaurant restaurant = new Restaurant();
        restaurant.setOwner(owner);
        restaurant.setName("Spice Route Kitchen");
        restaurant.setCuisineType("Indian");
        restaurant.setAddress("12 Market Street, London");
        restaurant.setPhone("020 7946 1234");
        restaurant.setDescription("Fresh curries, grilled specials, and fast delivery.");
        restaurant.setRating(4.7);
        restaurant.setActive(true);
        restaurant = restaurantRepository.save(restaurant);

        MenuItem item1 = new MenuItem();
        item1.setRestaurant(restaurant);
        item1.setName("Chicken Tikka Masala");
        item1.setDescription("Creamy tomato curry with basmati rice.");
        item1.setPrice(new BigDecimal("12.99"));
        item1.setCategory("Mains");
        item1.setTodaysSpecial(true);
        item1.setPopular(true);
        menuItemRepository.save(item1);

        MenuItem item2 = new MenuItem();
        item2.setRestaurant(restaurant);
        item2.setName("Paneer Wrap");
        item2.setDescription("Grilled paneer, salad, and mint chutney.");
        item2.setPrice(new BigDecimal("8.49"));
        item2.setCategory("Wraps");
        item2.setDealOfDay(true);
        menuItemRepository.save(item2);

        MenuItem item3 = new MenuItem();
        item3.setRestaurant(restaurant);
        item3.setName("Mango Lassi");
        item3.setDescription("Chilled yogurt drink with mango pulp.");
        item3.setPrice(new BigDecimal("3.75"));
        item3.setCategory("Drinks");
        menuItemRepository.save(item3);
    }
}