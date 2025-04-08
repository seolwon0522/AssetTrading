package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    String email;
    String password;
    String businessNumber;
    String companyName;
    String address;
    String industry;
    String phoneNumber;
    String description;
    public User toEntity(){
        return User.builder()
                .email(this.email)
                .password(this.password)
                .businessNumber(this.businessNumber)
                .companyName(this.companyName)
                .address(this.address)
                .industry(this.industry)
                .phoneNumber(this.phoneNumber)
                .description(this.description)
                .build();

    }

}
