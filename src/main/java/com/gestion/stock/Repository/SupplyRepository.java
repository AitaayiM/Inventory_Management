package com.gestion.stock.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.stock.entity.SupplyItem;

public interface SupplyRepository extends JpaRepository<SupplyItem, Integer> {
    Optional<SupplyItem> findByItemCodeAndDateExpectedDelivery(int ItemCode, LocalDate dateExpectedDelivery);

    List<SupplyItem> findByDateExpectedDelivery(LocalDate date);

    List<SupplyItem> findAll();
}
