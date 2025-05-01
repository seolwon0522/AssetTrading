package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long user_idx;

    @Column(name = "user_id") 
    private String userId;

    @Column(name = "user_pw")
    private String userPw;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "business_num")
    private String businessNum;

    @Column(name = "company_name")
    private String companyName; 

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_industry")
    private String companyIndustry;

    @Column(name = "company_tel")
    private String companyTell;

    @Transient
    private String description;

    @Column(name = "join_approved")
    private boolean joinApproved;

    @Column(name = "registered_at", updatable = false, insertable = false)
    private LocalDateTime registeredAt;
}
