package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long id;
    String email;
    String password;
    String businessNumber;
    String companyName;
    String adress;
    String industry;
    String phoneNumber;
    String description;
    boolean approved;

}
