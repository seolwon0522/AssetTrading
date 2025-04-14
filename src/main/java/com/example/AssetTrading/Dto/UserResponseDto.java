package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String userId;
    private String companyName;
    private boolean joinApproved;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .companyName(user.getCompanyName())
                .joinApproved(user.isJoinApproved())
                .build();
    }
}