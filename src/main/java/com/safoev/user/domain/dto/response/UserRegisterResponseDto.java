package com.safoev.user.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterResponseDto {
    private String firstName;
    private String phone;
}
