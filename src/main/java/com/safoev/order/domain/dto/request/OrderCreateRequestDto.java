package com.safoev.order.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderCreateRequestDto {

    private String telegramUsername;

    @NotBlank(message = "Имя клиента обязательно")
    private String customerName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат телефона")
    private String customerPhone;

    @NotBlank(message = "Описание услуги обязательно")
    private String orderDetails;
}