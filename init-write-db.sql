-- Replication User Setup
CREATE USER 'repl'@'%' IDENTIFIED BY '1234';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
FLUSH PRIVILEGES;

-- Schema Setup
CREATE DATABASE IF NOT EXISTS catch_dining;

USE catch_dining;

CREATE TABLE IF NOT EXISTS restaurant
(
    restaurant_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    city               VARCHAR(255),
    detail             VARCHAR(255),
    district           VARCHAR(255),
    province           VARCHAR(255),
    street             VARCHAR(255),
    country_type       VARCHAR(255),
    description        VARCHAR(255),
    food_type          VARCHAR(255),
    name               VARCHAR(255) UNIQUE,
    phone_number       VARCHAR(255),
    rating             DOUBLE,
    serving_type       VARCHAR(255),
    avg_rating         DOUBLE DEFAULT 0.0,
    review_count       INT DEFAULT 0,
    version            INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `user`
(
    user_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    fcm_token          VARCHAR(255),
    password           VARCHAR(255),
    phone_number       VARCHAR(255) UNIQUE,
    user_type          VARCHAR(255),
    username           VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS seat
(
    seat_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    available_date     DATE,
    available_quantity INT,
    available_time     TIME,
    max_head_count     INT,
    min_head_count     INT,
    quantity           INT,
    seat_type          VARCHAR(255),
    restaurant_id      BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id)
);

CREATE TABLE IF NOT EXISTS menu
(
    menu_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    name               VARCHAR(255),
    price              INT,
    restaurant_id      BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id)
);

CREATE TABLE IF NOT EXISTS payment
(
    payment_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    payment_status     VARCHAR(255),
    payment_type       VARCHAR(255),
    tid                VARCHAR(255),
    total_price        INT
);

CREATE TABLE IF NOT EXISTS reserve_menu
(
    reserve_menu_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    quantity           INT,
    reserve_price      INT,
    menu_name          VARCHAR(255),
    payment_id         BIGINT,
    FOREIGN KEY (payment_id) REFERENCES payment (payment_id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    reservation_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    head_count         INT,
    status             VARCHAR(255),
    time               DATETIME(6),
    reservation_status VARCHAR(255),
    restaurant_id      BIGINT,
    payment_id         BIGINT,
    user_id            BIGINT,
    seat_id            BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id),
    FOREIGN KEY (payment_id) REFERENCES payment (payment_id),
    FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    FOREIGN KEY (seat_id) REFERENCES seat (seat_id)
);

CREATE TABLE IF NOT EXISTS notification_request
(
    notification_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    desired_date            DATE,
    dining_period           VARCHAR(255),
    head_count              INT,
    restaurant_id           BIGINT,
    user_id                 BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id),
    FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS favorite
(
    favorite_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    restaurant_id      BIGINT,
    user_id            BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id),
    FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE IF NOT EXISTS review
(
    review_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    content            VARCHAR(255),
    mood_rating        DOUBLE,
    service_rating     DOUBLE,
    taste_rating       DOUBLE,
    user_id            BIGINT,
    restaurant_id      BIGINT,
    FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (restaurant_id)
);

CREATE TABLE IF NOT EXISTS review_comment
(
    review_comment_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_date       DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    content            VARCHAR(255),
    review_id          BIGINT,
    user_id            BIGINT,
    FOREIGN KEY (review_id) REFERENCES review (review_id),
    FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);