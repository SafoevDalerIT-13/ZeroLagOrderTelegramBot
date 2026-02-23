package com.safoev.user.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDto {
    private Long id;

    private Long telegramId;

    @NotBlank(message = "Обязательно для связи")
    private String telegramUserName;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат телефона")
    private String phone;

    public void setTelegramUsername(String telegramUser) {
        this.telegramUserName = telegramUser;
    }

    public String getTelegramUsername() {
        return telegramUserName;
    }
}
