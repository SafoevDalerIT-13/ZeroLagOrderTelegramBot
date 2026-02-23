package com.safoev.order.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatistics {
    private long totalOrders;
    private int activeOrders;
    private int completedOrders;
}