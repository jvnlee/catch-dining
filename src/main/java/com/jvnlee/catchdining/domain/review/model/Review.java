package com.jvnlee.catchdining.domain.review.model;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.undeveloped.ReviewComment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "taste_rating")
    private double tasteRating;

    @Column(name = "mood_rating")
    private double moodRating;

    @Column(name = "service_rating")
    private double serviceRating;

    private String content;

    @OneToMany(mappedBy = "review")
    private List<ReviewComment> reviewComments = new ArrayList<>();

    public Review(User user, Restaurant restaurant, double tasteRating, double moodRating, double serviceRating, String content) {
        this.user = user;
        this.restaurant = restaurant;
        this.tasteRating = tasteRating;
        this.moodRating = moodRating;
        this.serviceRating = serviceRating;
        this.content = content;
    }

}
