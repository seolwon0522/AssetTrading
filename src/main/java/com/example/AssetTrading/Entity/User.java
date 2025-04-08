package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long id;
    String email;
    String password;
    String businessNumber;
    String companyName;
    String address;
    String industry;
    String phoneNumber;
    String description;
    boolean approved;

}
