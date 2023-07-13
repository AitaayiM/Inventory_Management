package com.gestion.stock.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class SupplyItem {

     @EqualsAndHashCode.Include
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int itemCode;
    
     private int QuantityOrdered;
    
     @EqualsAndHashCode.Include
     private LocalDate dateExpectedDelivery;
    
     private String imageUrl;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image itemImage;

}
