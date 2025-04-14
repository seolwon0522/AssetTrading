package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long id;
    String user_id; //email
    String user_pw;
    String business_num;
    String company_name;
    String company_address;
    String company_industry;
    String company_tell;
    String description;
    boolean join_approved;
}
