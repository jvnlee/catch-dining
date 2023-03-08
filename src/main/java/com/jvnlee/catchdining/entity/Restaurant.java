package com.jvnlee.catchdining.entity;

import com.sun.jdi.PrimitiveValue;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Restaurant {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    private String phoneNumber;

    private String description;

    private double rating;

    @OneToMany(mappedBy = "restaurant")
    private List<Seat> seats;

    @Enumerated(EnumType.STRING)
    private CountryType countryType;

    @Enumerated(EnumType.STRING)
    private FoodType foodType;

    @Enumerated(EnumType.STRING)
    private ServingType servingType;

    @OneToMany(mappedBy = "restaurant")
    private List<Menu> menus;

    @OneToMany(mappedBy = "restaurant")
    private List<Review> reviews;

}
