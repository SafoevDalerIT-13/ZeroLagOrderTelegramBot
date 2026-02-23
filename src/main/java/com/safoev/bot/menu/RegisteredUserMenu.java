package com.safoev.bot.menu;

import com.safoev.bot.handler.MessageSender;
import com.safoev.bot.keyboard.KeyboardFactory;
import com.safoev.order.domain.dto.OrderStatistics;
import com.safoev.order.domain.service.OrderService;
import com.safoev.user.domain.db.UserEntity;
import com.safoev.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisteredUserMenu {

    private final MessageSender messageSender;
    private final KeyboardFactory keyboardFactory;
    private final UserService userService;
    private final OrderService orderService;

    public void showProfile(Long chatId, User telegramUser) {
        UserEntity user = userService.findByTelegramId(chatId);
        if (user == null) {
            messageSender.send(chatId, "❌ Профиль не найден!");
            return;
        }

        String telegramUsername = telegramUser.getUserName();
        OrderStatistics stats = orderService.getUserStatisticsUniversal(chatId, telegramUsername);

        String profile = String.format("""
            👤 ВАШ ПРОФИЛЬ
            ─────────────────────
            
            📝 Имя: %s
            📱 Телефон: %s
            🆔 Telegram: @%s
            
            📊 СТАТИСТИКА ЗАКАЗОВ:
            • Всего заказов: %d
            • Активных: %d
            • Выполнено: %d
            
            📅 Зарегистрирован: %s
            """,
                user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : ""),
                user.getPhone() != null ? user.getPhone() : "не указан",
                user.getTelegramUserName() != null ? user.getTelegramUserName() : "не указан",
                stats.getTotalOrders(),
                stats.getActiveOrders(),
                stats.getCompletedOrders(),
                formatDate(user.getRegisteredAt())
        );

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("◀️ Назад в меню")
                .callbackData("back_to_menu")
                .build();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(new InlineKeyboardRow(backButton))
        );

        messageSender.sendWithKeyboard(chatId, profile, keyboard);
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "неизвестно";
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}