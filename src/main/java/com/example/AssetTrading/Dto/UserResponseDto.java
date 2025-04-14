package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String user_id;
    private String company_name;
    private boolean join_approved;

    // Entity → DTO 변환
    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .user_id(user.getUser_id())
                .company_name(user.getCompany_name())
                .join_approved(user.isJoin_approved())
                .build();
    }
}
