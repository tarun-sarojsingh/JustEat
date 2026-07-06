package com.justeat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @Column(name = "is_todays_special")
    private boolean isTodaysSpecial = false;

    @Column(name = "is_deal_of_day")
    private boolean isDealOfDay = false;

    @Column(name = "is_popular")
    private boolean isPopular = false;

    @Column(name = "order_count")
    private int orderCount = 0;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public MenuItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isTodaysSpecial() { return isTodaysSpecial; }
    public void setTodaysSpecial(boolean todaysSpecial) { isTodaysSpecial = todaysSpecial; }
    public boolean isDealOfDay() { return isDealOfDay; }
    public void setDealOfDay(boolean dealOfDay) { isDealOfDay = dealOfDay; }
    public boolean isPopular() { return isPopular; }
    public void setPopular(boolean popular) { isPopular = popular; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
