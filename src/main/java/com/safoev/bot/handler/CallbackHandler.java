package com.safoev.bot.handler;

import com.safoev.bot.menu.MenuService;
import com.safoev.bot.menu.RegisteredUserMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final MenuService menuService;
    private final RegisteredUserMenu registeredUserMenu;
    private final RegistrationHandler registrationHandler;
    private final OrderHandler orderHandler;

    public void handle(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var telegramUser = callbackQuery.getFrom();

        log.debug("Callback {} от {}", data, chatId);

        // Обработка callback'ов заказов
        if (data.startsWith("view_order_")) {
            String orderNumber = data.substring(11);
            orderHandler.viewOrderDetails(chatId, orderNumber);
            return;
        } else if (data.startsWith("confirm_cancel_")) {
            String orderNumber = data.substring(15);
            orderHandler.cancelOrder(chatId, orderNumber);
            return;
        }

        switch (data) {
            case "registration":
                registrationHandler.start(chatId, telegramUser);
                break;
            case "non_registration":
                menuService.sendUnauthorizedMenu(chatId);
                break;
            case "cancel_registration":
                registrationHandler.cancel(chatId);
                break;
            case "new_order":
                orderHandler.startCreation(chatId, telegramUser);
                break;
            case "my_orders":
                orderHandler.showMyOrders(chatId, telegramUser);
                break;
            case "my_profile":
                registeredUserMenu.showProfile(chatId, telegramUser);
                break;
            case "help":
                menuService.sendHelp(chatId);
                break;
            case "back_to_menu":
                menuService.sendAuthorizedMenu(chatId);
                break;
            default:
                log.warn("Неизвестный callback: {}", data);
        }
    }
}