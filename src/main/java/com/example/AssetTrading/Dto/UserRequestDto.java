package com.example.AssetTrading.Dto;

import com.example.AssetTrading.Entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String user_id;
    private String user_pw;
    private String business_num;
    private String company_name;
    private String company_address;
    private String company_industry;
    private String company_tell;

    public User toEntity() {
        return User.builder()
                .user_id(this.user_id)
                .user_pw(this.user_pw)
                .business_num(this.business_num)
                .company_name(this.company_name)
                .company_address(this.company_address)
                .company_industry(this.company_industry)
                .company_tell(this.company_tell)
                .build();
    }
}
