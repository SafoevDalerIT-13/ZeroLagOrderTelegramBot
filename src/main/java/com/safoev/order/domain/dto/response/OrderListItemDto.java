package com.safoev.order.domain.dto.response;

import com.safoev.order.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderListItemDto {
    private String orderNumber;
    private String orderDetails;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String telegramUserName;
}