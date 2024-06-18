CREATE DATABASE IF NOT EXISTS catch_dining;

USE catch_dining;

CREATE TABLE IF NOT EXISTS restaurant_review_stat
(
    restaurant_review_stat_id BIGINT PRIMARY KEY,
    created_date              DATETIME(6) NOT NULL,
    last_modified_date        DATETIME(6) NOT NULL,
    name                      VARCHAR(255) UNIQUE,
    city                      VARCHAR(255),
    detail                    VARCHAR(255),
    district                  VARCHAR(255),
    province                  VARCHAR(255),
    street                    VARCHAR(255),
    phone_number              VARCHAR(20),
    description               VARCHAR(255),
    avg_rating                DOUBLE,
    review_count              INT,
    country_type              VARCHAR(255),
    food_type                 VARCHAR(255),
    serving_type              VARCHAR(255),
    version                   INT
);