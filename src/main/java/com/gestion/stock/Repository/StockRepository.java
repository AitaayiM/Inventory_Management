package com.gestion.stock.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.gestion.stock.entity.ItemStock;

public interface StockRepository extends JpaRepository<ItemStock, Integer>{
}