package com.safoev.bot.handler;

import com.safoev.bot.keyboard.KeyboardFactory;
import com.safoev.bot.menu.MenuService;
import com.safoev.order.domain.dto.request.OrderCreateRequestDto;
import com.safoev.order.domain.dto.response.OrderDetailDto;
import com.safoev.order.domain.dto.response.OrderListItemDto;
import com.safoev.order.domain.dto.response.OrderResponseDto;
import com.safoev.order.domain.enums.OrderStatus;
import com.safoev.order.domain.exception.OrderNotFoundException;
import com.safoev.order.domain.service.OrderService;
import com.safoev.order.util.OrderFormatter;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final MessageSender messageSender;
    private final KeyboardFactory keyboardFactory;
    private final MenuService menuService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderFormatter orderFormatter;

    private final Map<Long, TempOrderData> orderData = new ConcurrentHashMap<>();
    private final Map<Long, String> orderStep = new ConcurrentHashMap<>();

    public void startCreation(Long chatId, User telegramUser) {
        String telegramUsername = telegramUser.getUserName();

        TempOrderData data = new TempOrderData();
        data.telegramUsername = telegramUsername;
        orderData.put(chatId, data);

        if (userService.existsByTelegramId(chatId)) {
            // Зарегистрированный пользователь
            UserEntity user = userService.findByTelegramId(chatId);
            data.customerName = user.getFirstName() +
                    (user.getLastName() != null ? " " + user.getLastName() : "");
            data.customerPhone = user.getPhone();

            orderStep.put(chatId, "ENTER_SERVICE");

            messageSender.sendWithKeyboard(
                    chatId,
                    "📦 Опишите услугу, которую хотите заказать:",
                    keyboardFactory.createCancelKeyboard()
            );
        } else {
            // Незарегистрированный пользователь
            orderStep.put(chatId, "ENTER_NAME");

            messageSender.sendWithKeyboard(
                    chatId,
                    "📦 Введите ваше имя:",
                    keyboardFactory.createCancelKeyboard()
            );
        }
    }

    public void handleDataInput(Long chatId, String input, User telegramUser) {
        if (input.equals("❌ Отмена")) {
            cancelCreation(chatId);
            return;
        }

        String currentStep = orderStep.get(chatId);
        TempOrderData data = orderData.get(chatId);

        try {
            switch (currentStep) {
                case "ENTER_NAME":
                    data.customerName = input;
                    orderStep.put(chatId, "ENTER_LAST_NAME");
                    messageSender.send(chatId, "Введите фамилию (или '-' чтобы пропустить):");
                    break;

                case "ENTER_LAST_NAME":
                    if (!input.equals("-")) {
                        data.customerLastName = input;
                        data.customerName = data.customerName + " " + input;
                    }
                    orderStep.put(chatId, "ENTER_PHONE");
                    messageSender.send(chatId, "📱 Введите номер телефона (например: +79991234567):");
                    break;

                case "ENTER_PHONE":
                    if (!input.matches("^\\+?[0-9]{10,15}$")) {
                        messageSender.send(chatId, "❌ Неверный формат телефона. Используйте +79991234567:");
                        return;
                    }
                    data.customerPhone = input;
                    orderStep.put(chatId, "ENTER_SERVICE");
                    messageSender.send(chatId, "📦 Опишите услугу:");
                    break;

                case "ENTER_SERVICE":
                    completeOrder(chatId, input);
                    break;

                default:
                    cancelCreation(chatId);
            }
        } catch (Exception e) {
            log.error("Ошибка при создании заказа: {}", e.getMessage());
            messageSender.send(chatId, "❌ Произошла ошибка. Попробуйте еще раз.");
            cancelCreation(chatId);
        }
    }

    private void completeOrder(Long chatId, String serviceDescription) {
        try {
            TempOrderData data = orderData.get(chatId);

            OrderCreateRequestDto requestDto = new OrderCreateRequestDto();
            requestDto.setTelegramUsername(data.telegramUsername);
            requestDto.setCustomerName(data.customerName);
            requestDto.setCustomerPhone(data.customerPhone);
            requestDto.setOrderDetails(serviceDescription);

            Long userId = userService.existsByTelegramId(chatId) ? chatId : null;
            OrderResponseDto createdOrder = orderService.createOrder(userId, data.telegramUsername, requestDto);

            orderData.remove(chatId);
            orderStep.remove(chatId);

            String successMessage = String.format("""
                    ✅ Заказ успешно создан!
                    
                    🆔 Номер: %s
                    📝 Услуга: %s
                    📊 Статус: %s
                    """,
                    createdOrder.getOrderNumber(),
                    createdOrder.getOrderDetails(),
                    createdOrder.getStatus().getDisplayName()
            );

            messageSender.send(chatId, successMessage);

            menuService.sendAuthorizedMenu(chatId);
            //menuService.sendMainMenu(chatId, null);

        } catch (Exception e) {
            log.error("Ошибка создания заказа: {}", e.getMessage());
            messageSender.send(chatId, "❌ Ошибка: " + e.getMessage());
            cancelCreation(chatId);
        }
    }

    public void showMyOrders(Long chatId, User telegramUser) {
        Long userId = userService.existsByTelegramId(chatId) ? chatId : null;
        String telegramUsername = telegramUser.getUserName();

        List<OrderListItemDto> orders = orderService.getUserOrders(userId, telegramUsername);

        StringBuilder message = new StringBuilder("📋 Ваши заказы:\n\n");

        if (orders.isEmpty()) {
            message.append("У вас пока нет заказов.");
        } else {
            for (int i = 0; i < orders.size(); i++) {
                OrderListItemDto order = orders.get(i);
                message.append(i + 1).append(". 🆔 ").append(order.getOrderNumber()).append("\n");
                message.append("   📊 ").append(order.getStatus().getDisplayName()).append("\n");
                message.append("   📝 ").append(order.getOrderDetails()).append("\n");
                message.append("   📅 ").append(formatDate(order.getCreatedAt())).append("\n\n");
            }
            message.append("Чтобы посмотреть детали заказа, введите его номер:");
        }

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("◀️ Назад в меню")
                .callbackData("back_to_menu")
                .build();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(new InlineKeyboardRow(backButton))
        );

        messageSender.sendWithKeyboard(chatId, message.toString(), keyboard);
    }

    public void viewOrderDetails(Long chatId, String orderNumber) {
        log.debug("Просмотр деталей заказа: {} для пользователя {}", orderNumber, chatId);

        try {
            OrderDetailDto order = orderService.getOrderDetails(orderNumber);

            String details = String.format("""
                🆔 Номер: %s
                📅 Дата: %s
                📊 Статус: %s
                👤 Клиент: %s
                📞 Телефон: %s
                📝 Услуга: %s
                """,
                    order.getOrderNumber(),
                    formatDate(order.getCreatedAt()),
                    order.getStatus().getDisplayName(),
                    order.getCustomerName(),
                    order.getCustomerPhone(),
                    order.getOrderDetails()
            );

            if (order.getStatus() == OrderStatus.NEW) {
                messageSender.sendWithKeyboard(
                        chatId,
                        details,
                        keyboardFactory.createConfirmCancelKeyboard(orderNumber)
                );
            } else {
                messageSender.send(chatId, details);
            }

        } catch (OrderNotFoundException e) {
            log.error("Заказ не найден: {}", orderNumber);
            menuService.sendMessage(chatId, "❌ Заказ с номером " + orderNumber + " не найден!");
        } catch (Exception e) {
            log.error("Ошибка просмотра заказа: {}", e.getMessage());
            menuService.sendMessage(chatId, "❌ Произошла ошибка при просмотре заказа");
        }
    }

    public void cancelOrder(Long chatId, String orderNumber) {
        try {
            OrderResponseDto order = orderService.updateOrderStatus(orderNumber, OrderStatus.CANCELLED);
            messageSender.send(chatId, "✅ Заказ успешно отменен.");
            viewOrderDetails(chatId, orderNumber);
        } catch (Exception e) {
            messageSender.send(chatId, "❌ Ошибка при отмене заказа: " + e.getMessage());
        }
    }

    public boolean isInProgress(Long chatId) {
        return orderStep.containsKey(chatId);
    }

    private void cancelCreation(Long chatId) {
        orderData.remove(chatId);
        orderStep.remove(chatId);
        messageSender.send(chatId, "❌ Создание заказа отменено.");
        menuService.sendMainMenu(chatId, null);
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "неизвестно";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private static class TempOrderData {
        String telegramUsername;
        String customerName;
        String customerLastName;
        String customerPhone;
    }
}