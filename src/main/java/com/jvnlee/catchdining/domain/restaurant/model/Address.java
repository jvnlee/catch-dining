package com.jvnlee.catchdining.domain.restaurant.model;

import javax.persistence.Embeddable;

@Embeddable
public class Address {

    private String province;

    private String city;

    private String district;

    private String street;

    private String detail;

}
