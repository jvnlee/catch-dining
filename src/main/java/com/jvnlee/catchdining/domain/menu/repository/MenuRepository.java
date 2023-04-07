package com.jvnlee.catchdining.domain.menu.repository;

import com.jvnlee.catchdining.domain.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
