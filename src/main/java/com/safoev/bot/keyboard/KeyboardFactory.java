package com.safoev.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup createMainMenuForUnauthorized() {
        var newOrderButton = InlineKeyboardButton.builder()
                .text("📦 Создать заказ")
                .callbackData("new_order")
                .build();

        var registerButton = InlineKeyboardButton.builder()
                .text("📝 Зарегистрироваться")
                .callbackData("registration")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(newOrderButton),
                new InlineKeyboardRow(registerButton)
        );

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup createMainMenuForAuthorized() {
        var newOrderButton = InlineKeyboardButton.builder()
                .text("📦 Создать заказ")
                .callbackData("new_order")
                .build();

        var myOrdersButton = InlineKeyboardButton.builder()
                .text("📋 Мои заказы")
                .callbackData("my_orders")
                .build();

        var profileButton = InlineKeyboardButton.builder()
                .text("👤 Мой профиль")
                .callbackData("my_profile")
                .build();

        var helpButton = InlineKeyboardButton.builder()
                .text("ℹ️ Помощь")
                .callbackData("help")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(newOrderButton),
                new InlineKeyboardRow(myOrdersButton),
                new InlineKeyboardRow(profileButton),
                new InlineKeyboardRow(helpButton)
        );

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup createOrderOrRegisterKeyboard() {
        var newOrderButton = InlineKeyboardButton.builder()
                .text("📦 Создать заказ")
                .callbackData("new_order")
                .build();

        var registerButton = InlineKeyboardButton.builder()
                .text("📝 Зарегистрироваться")
                .callbackData("registration")
                .build();

        var backButton = InlineKeyboardButton.builder()
                .text("◀️ Назад")
                .callbackData("back_to_menu")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(newOrderButton),
                new InlineKeyboardRow(registerButton),
                new InlineKeyboardRow(backButton)
        );

        return new InlineKeyboardMarkup(rows);
    }

    public ReplyKeyboardMarkup createCancelKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add("❌ Отмена");

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    public InlineKeyboardMarkup createBackToMenuKeyboard() {
        var backButton = InlineKeyboardButton.builder()
                .text("◀️ Назад в меню")
                .callbackData("back_to_menu")
                .build();

        return new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(backButton)));
    }

    public InlineKeyboardMarkup createOrderActionsKeyboard(String orderNumber) {
        var viewButton = InlineKeyboardButton.builder()
                .text("👁 Детали заказа")
                .callbackData("view_order_" + orderNumber)
                .build();

        return new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(viewButton)));
    }

    public InlineKeyboardMarkup createConfirmCancelKeyboard(String orderNumber) {
        var confirmButton = InlineKeyboardButton.builder()
                .text("✅ Да, отменить")
                .callbackData("confirm_cancel_" + orderNumber)
                .build();

        var backButton = InlineKeyboardButton.builder()
                .text("◀️ Назад к заказам")
                .callbackData("my_orders")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(confirmButton),
                new InlineKeyboardRow(backButton)
        );

        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup createOrderDetailsBackKeyboard(String orderNumber) {
        var backToOrdersButton = InlineKeyboardButton.builder()
                .text("◀️ К списку заказов")
                .callbackData("my_orders")
                .build();

        var backToMenuButton = InlineKeyboardButton.builder()
                .text("🏠 В меню")
                .callbackData("back_to_menu")
                .build();

        List<InlineKeyboardRow> rows = List.of(
                new InlineKeyboardRow(backToOrdersButton),
                new InlineKeyboardRow(backToMenuButton)
        );

        return new InlineKeyboardMarkup(rows);
    }


}