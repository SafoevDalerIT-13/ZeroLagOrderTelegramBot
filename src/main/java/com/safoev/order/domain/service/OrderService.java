package com.safoev.order.domain.service;

import com.safoev.order.domain.db.OrderEntity;
import com.safoev.order.domain.db.OrderRepository;
import com.safoev.order.domain.dto.OrderStatistics;
import com.safoev.order.domain.dto.request.OrderCreateRequestDto;
import com.safoev.order.domain.dto.response.OrderDetailDto;
import com.safoev.order.domain.dto.response.OrderListItemDto;
import com.safoev.order.domain.dto.response.OrderResponseDto;
import com.safoev.order.domain.enums.OrderStatus;
import com.safoev.order.domain.exception.OrderCreationException;
import com.safoev.order.domain.exception.OrderNotFoundException;
import com.safoev.user.domain.db.UserEntity;
import com.safoev.user.domain.db.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponseDto createOrder(Long userId, String telegramUsername, OrderCreateRequestDto requestDto) {
        log.info("Создание заказа. UserId: {}, TelegramUsername: {}", userId, telegramUsername);

        try {
            String orderNumber = generateOrderNumber();

            UserEntity user = null;
            if (userId != null) {
                user = userRepository.findByTelegramId(userId).orElse(null);
            }

            OrderEntity order = OrderEntity.builder()
                    .orderNumber(orderNumber)
                    .user(user)
                    .telegramUsername(telegramUsername)
                    .customerName(requestDto.getCustomerName())
                    .customerPhone(requestDto.getCustomerPhone())
                    .orderDetails(requestDto.getOrderDetails())
                    .status(OrderStatus.NEW)
                    .build();

            OrderEntity savedOrder = orderRepository.save(order);
            log.info("Заказ создан. Номер: {}", savedOrder.getOrderNumber());

            return mapToResponseDto(savedOrder);

        } catch (Exception e) {
            log.error("Ошибка создания заказа: {}", e.getMessage());
            throw new OrderCreationException("Не удалось создать заказ: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetails(String orderNumber) {
        log.debug("Получение деталей заказа: {}", orderNumber);

        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + orderNumber));

        return OrderDetailDto.builder()
                .orderNumber(order.getOrderNumber())
                .telegramUsername(order.getTelegramUsername())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .orderDetails(order.getOrderDetails())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderListItemDto> getUserOrders(Long userId, String telegramUsername) {
        log.debug("Получение заказов. UserId: {}, TelegramUsername: {}", userId, telegramUsername);

        List<OrderEntity> orders;

        if (userId != null) {
            // Зарегистрированный пользователь - ищем по userId
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.debug("Найдено заказов по userId {}: {}", userId, orders.size());
        } else {
            // Незарегистрированный пользователь - ищем по telegramUsername
            orders = orderRepository.findByTelegramUsernameOrderByCreatedAtDesc(telegramUsername);
            log.debug("Найдено заказов по telegramUsername {}: {}", telegramUsername, orders.size());
        }

        // Если заказы не найдены, попробуем найти все заказы для этого пользователя
        if (orders.isEmpty() && userId != null) {
            orders = orderRepository.findByTelegramUsernameOrderByCreatedAtDesc(telegramUsername);
            log.debug("Повторный поиск по telegramUsername {}: {}", telegramUsername, orders.size());
        }

        return orders.stream()
                .map(this::mapToListItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderStatistics getUserStatistics(Long userId, String telegramUsername) {
        log.debug("Получение статистики. UserId: {}, TelegramUsername: {}", userId, telegramUsername);

        long totalOrders;
        long activeOrders;
        long completedOrders;

        if (userId != null) {
            // Зарегистрированный пользователь - считаем по userId
            totalOrders = orderRepository.countTotalByUserId(userId);
            activeOrders = orderRepository.countActiveByUserId(userId, OrderStatus.NEW);
            completedOrders = orderRepository.countCompletedByUserId(userId, OrderStatus.COMPLETED);
            log.debug("Статистика по userId: всего={}, активных={}, выполнено={}", totalOrders, activeOrders, completedOrders);
        } else {
            // Незарегистрированный пользователь - считаем по telegramUsername
            totalOrders = orderRepository.countTotalByTelegramUsername(telegramUsername);
            activeOrders = orderRepository.countActiveByTelegramUsername(telegramUsername, OrderStatus.NEW);
            completedOrders = orderRepository.countCompletedByTelegramUsername(telegramUsername, OrderStatus.COMPLETED);
            log.debug("Статистика по telegramUsername: всего={}, активных={}, выполнено={}", totalOrders, activeOrders, completedOrders);
        }

        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .activeOrders((int) activeOrders)
                .completedOrders((int) completedOrders)
                .build();
    }

    // Или используйте универсальный метод:
    @Transactional(readOnly = true)
    public OrderStatistics getUserStatisticsUniversal(Long userId, String telegramUsername) {
        log.debug("Получение статистики (универсально). UserId: {}, TelegramUsername: {}", userId, telegramUsername);

        long totalOrders = orderRepository.countTotalUserOrders(userId, telegramUsername);
        long activeOrders = orderRepository.countActiveUserOrders(userId, telegramUsername, OrderStatus.NEW);
        long completedOrders = orderRepository.countCompletedUserOrders(userId, telegramUsername, OrderStatus.COMPLETED);

        log.debug("Универсальная статистика: всего={}, активных={}, выполнено={}", totalOrders, activeOrders, completedOrders);

        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .activeOrders((int) activeOrders)
                .completedOrders((int) completedOrders)
                .build();
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        log.info("Обновление статуса заказа {} на {}", orderNumber, newStatus);

        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Заказ не найден: " + orderNumber));

        order.setStatus(newStatus);
        OrderEntity updatedOrder = orderRepository.save(order);

        return mapToResponseDto(updatedOrder);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }

    private OrderResponseDto mapToResponseDto(OrderEntity order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .telegramUsername(order.getTelegramUsername())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .orderDetails(order.getOrderDetails())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderListItemDto mapToListItemDto(OrderEntity order) {
        return OrderListItemDto.builder()
                .orderNumber(order.getOrderNumber())
                .orderDetails(truncate(order.getOrderDetails(), 50))
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String truncate(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }
}