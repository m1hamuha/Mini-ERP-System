package com.altenburg.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;       // Название товара
    private int quantity;      // Menge (Количество)
    private double price;      // Preis (Цена)
}
