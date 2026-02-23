package com.safoev.order.util;

import com.safoev.order.domain.dto.response.OrderDetailDto;
import com.safoev.order.domain.dto.response.OrderListItemDto;
import com.safoev.order.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class OrderFormatter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String formatOrderDetails(OrderDetailDto order) {
        StringBuilder sb = new StringBuilder();

        sb.append("┌─────────────────────┐\n");
        sb.append("│   ДЕТАЛИ ЗАКАЗА     │\n");
        sb.append("└─────────────────────┘\n\n");

        sb.append("🆔 Номер: ").append(order.getOrderNumber()).append("\n");
        sb.append("📅 Дата: ").append(formatDate(order.getCreatedAt())).append("\n");
        sb.append("📊 Статус: ").append(formatStatus(order.getStatus())).append("\n");
        sb.append("─────────────────────\n");

        if (order.getTelegramUsername() != null && !order.getTelegramUsername().isEmpty()) {
            sb.append("📱 Telegram: @").append(order.getTelegramUsername()).append("\n");
        }

        sb.append("👤 Клиент: ").append(order.getCustomerName()).append("\n");
        sb.append("📞 Телефон: ").append(order.getCustomerPhone()).append("\n");
        sb.append("─────────────────────\n");
        sb.append("📝 Услуга:\n").append(order.getOrderDetails()).append("\n");

        return sb.toString();
    }

    public String formatOrderListItem(OrderListItemDto order) {
        return String.format("""
                🆔 %s
                📊 %s
                📝 %s
                📅 %s
                """,
                order.getOrderNumber(),
                formatStatus(order.getStatus()),
                order.getOrderDetails(),
                formatDateShort(order.getCreatedAt())
        );
    }

    public String formatOrderListHeader(int totalOrders) {
        return String.format("""
                ┌─────────────────────┐
                │   ВАШИ ЗАКАЗЫ       │
                └─────────────────────┘
                
                📋 Всего заказов: %d
                
                """, totalOrders);
    }

    public String formatEmptyOrderList() {
        return """
                📋 У вас пока нет заказов.
                
                Хотите создать первый заказ?
                """;
    }

    private String formatStatus(OrderStatus status) {
        switch (status) {
            case NEW: return "🆕 Новый";
            case IN_PROGRESS: return "⚙️ В работе";
            case COMPLETED: return "✅ Выполнен";
            case CANCELLED: return "❌ Отменен";
            default: return status.name();
        }
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "неизвестно";
        return date.format(DATE_FORMATTER);
    }

    private String formatDateShort(java.time.LocalDateTime date) {
        if (date == null) return "неизвестно";
        return date.format(DATE_ONLY_FORMATTER);
    }
}