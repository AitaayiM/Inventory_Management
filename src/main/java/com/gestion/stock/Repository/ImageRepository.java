package com.gestion.stock.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.stock.entity.Image;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findById(long id);
}
