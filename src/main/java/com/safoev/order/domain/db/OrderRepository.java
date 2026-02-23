package com.safoev.order.domain.db;

import com.safoev.order.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Поиск по номеру заказа
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    // Все заказы пользователя по userId (для зарегистрированных)
    @Query("SELECT o FROM OrderEntity o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Все заказы по telegramUsername (для всех пользователей)
    List<OrderEntity> findByTelegramUsernameOrderByCreatedAtDesc(String telegramUsername);

    // УНИВЕРСАЛЬНЫЙ МЕТОД - ищем по userId ИЛИ по telegramUsername
    @Query("SELECT o FROM OrderEntity o WHERE (o.user.id = :userId OR o.telegramUsername = :telegramUsername) ORDER BY o.createdAt DESC")
    List<OrderEntity> findUserOrders(@Param("userId") Long userId, @Param("telegramUsername") String telegramUsername);

    // ============= МЕТОДЫ ДЛЯ ПОДСЧЕТА =============

    // Общее количество заказов пользователя (по userId)
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.user.id = :userId")
    long countTotalByUserId(@Param("userId") Long userId);

    // Общее количество заказов по telegramUsername
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.telegramUsername = :telegramUsername")
    long countTotalByTelegramUsername(@Param("telegramUsername") String telegramUsername);

    // УНИВЕРСАЛЬНЫЙ метод подсчета всех заказов (по userId ИЛИ telegramUsername)
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.user.id = :userId OR o.telegramUsername = :telegramUsername")
    long countTotalUserOrders(@Param("userId") Long userId, @Param("telegramUsername") String telegramUsername);

    // Количество активных заказов (NEW) по userId
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.user.id = :userId AND o.status = :status")
    long countActiveByUserId(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // Количество активных заказов (NEW) по telegramUsername
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.telegramUsername = :telegramUsername AND o.status = :status")
    long countActiveByTelegramUsername(@Param("telegramUsername") String telegramUsername, @Param("status") OrderStatus status);

    // УНИВЕРСАЛЬНЫЙ метод подсчета активных заказов
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE (o.user.id = :userId OR o.telegramUsername = :telegramUsername) AND o.status = :status")
    long countActiveUserOrders(@Param("userId") Long userId, @Param("telegramUsername") String telegramUsername, @Param("status") OrderStatus status);

    // Количество выполненных заказов (COMPLETED) по userId
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.user.id = :userId AND o.status = :status")
    long countCompletedByUserId(@Param("userId") Long userId, @Param("status") OrderStatus status);

    // Количество выполненных заказов (COMPLETED) по telegramUsername
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.telegramUsername = :telegramUsername AND o.status = :status")
    long countCompletedByTelegramUsername(@Param("telegramUsername") String telegramUsername, @Param("status") OrderStatus status);

    // УНИВЕРСАЛЬНЫЙ метод подсчета выполненных заказов
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE (o.user.id = :userId OR o.telegramUsername = :telegramUsername) AND o.status = :status")
    long countCompletedUserOrders(@Param("userId") Long userId, @Param("telegramUsername") String telegramUsername, @Param("status") OrderStatus status);
}