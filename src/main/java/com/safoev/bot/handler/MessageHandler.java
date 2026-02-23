package com.safoev.bot.handler;

import com.safoev.bot.menu.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final MenuService menuService;
    private final RegistrationHandler registrationHandler;
    private final OrderHandler orderHandler;

    public void handle(Message message) {
        var chatId = message.getChatId();
        var text = message.getText();
        var telegramUser = message.getFrom();

        log.debug("Сообщение от {}: {}", chatId, text);

        // Проверяем процессы
        if (registrationHandler.isInProgress(chatId)) {
            registrationHandler.handleInput(chatId, text, telegramUser);
            return;
        }

        if (orderHandler.isInProgress(chatId)) {
            orderHandler.handleDataInput(chatId, text, telegramUser);
            return;
        }

        // Проверяем, не является ли сообщение номером заказа (формат ORD-...)
        if (text.startsWith("ORD-") && text.length() > 4) {
            log.debug("Пользователь ввел номер заказа: {}", text);
            orderHandler.viewOrderDetails(chatId, text);
            return;
        }

        // Проверяем, не является ли сообщение числом (возможно пользователь ввел номер из списка)
        if (text.matches("\\d+")) {
            // Если пользователь ввел просто число, просим ввести полный номер
            menuService.sendMessage(chatId, """
                    Для просмотра деталей заказа введите полный номер заказа.
                    
                    Например: ORD-202602231551-04FD
                    
                    Чтобы вернуться в меню, нажмите /start
                    """);
            return;
        }

        // Команды
        switch (text) {
            case "/start":
                menuService.sendMainMenu(chatId, telegramUser);
                break;
            case "/help":
                menuService.sendHelp(chatId);
                break;
            default:
                menuService.sendUnknownCommand(chatId);
        }
    }
}