package com.safoev.user.domain.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByTelegramId(Long telegramId);

    boolean existsByPhone(String phone);

    Optional<UserEntity> findByTelegramId(Long telegramId);  // Добавляем этот метод

    Optional<UserEntity> findByPhone(String phone);
}