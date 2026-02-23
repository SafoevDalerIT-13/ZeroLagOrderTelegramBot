package com.safoev.order.domain.dto.response;

import com.safoev.order.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private String telegramUsername;
    private String customerName;
    private String customerPhone;
    private String orderDetails;
    private OrderStatus status;
    private LocalDateTime createdAt;
}