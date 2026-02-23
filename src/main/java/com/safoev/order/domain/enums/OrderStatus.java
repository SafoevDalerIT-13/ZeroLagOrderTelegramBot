package com.safoev.order.domain.enums;

public enum OrderStatus {
    NEW("🆕 Новый"),
    IN_PROGRESS("⚙️ В работе"),
    COMPLETED("✅ Выполнен"),
    CANCELLED("❌ Отменен");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}