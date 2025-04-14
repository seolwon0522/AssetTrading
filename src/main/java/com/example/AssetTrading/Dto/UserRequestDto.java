package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String userId;
    private String userPw;
    private String businessNum;
    private String companyName;
    private String companyAddress;
    private String companyIndustry;
    private String companyTell;

    public User toEntity() {
        return User.builder()
                .userId(userId)
                .userPw(userPw)
                .businessNum(businessNum)
                .companyName(companyName)
                .companyAddress(companyAddress)
                .companyIndustry(companyIndustry)
                .companyTell(companyTell)
                .build();
    }
}