package com.safoev.user.domain.service;

import com.safoev.user.domain.db.UserEntity;
import com.safoev.user.domain.db.UserRepository;
import com.safoev.user.domain.dto.request.UserRegisterRequestDto;
import com.safoev.user.domain.dto.response.UserRegisterResponseDto;
import com.safoev.user.domain.service.exception.UserAlreadyExistsException;
import com.safoev.user.domain.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor  // Заменяем @AllArgsConstructor на @RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    // Убираем CharacterEncodingFilter - он здесь не нужен

    @Transactional
    public UserRegisterResponseDto registerUser(Long telegramId, UserRegisterRequestDto requestDto) {
        log.info("Регистрация пользователя с Telegram ID: {}", telegramId);

        // Проверяем существование
        if (userRepository.existsByTelegramId(telegramId)) {
            throw new UserAlreadyExistsException("Пользователь с таким Telegram ID уже зарегистрирован!");
        }

        if (requestDto.getPhone() != null && userRepository.existsByPhone(requestDto.getPhone())) {
            throw new UserAlreadyExistsException("Этот номер телефона уже используется");
        }

        // Создаем пользователя
        UserEntity user = UserEntity.builder()
                .telegramId(telegramId)
                .telegramUserName(requestDto.getTelegramUsername())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .phone(requestDto.getPhone())
                .registeredAt(LocalDateTime.now())
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getTelegramId());

        return UserRegisterResponseDto.builder()
                .firstName(savedUser.getFirstName())
                .phone(savedUser.getPhone())
                .build();
    }

    // Реализация метода findByTelegramId
    @Transactional(readOnly = true)
    public UserEntity findByTelegramId(Long telegramId) {
        log.debug("Поиск пользователя по Telegram ID: {}", telegramId);

        return userRepository.findByTelegramId(telegramId)
                .orElse(null);  // Возвращаем null если не найден
    }

    // Альтернативный вариант с исключением
    @Transactional(readOnly = true)
    public UserEntity findByTelegramIdOrThrow(Long telegramId) {
        log.debug("Поиск пользователя по Telegram ID: {}", telegramId);

        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с Telegram ID " + telegramId + " не найден"));
    }

    @Transactional(readOnly = true)
    public boolean existsByTelegramId(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    @Transactional(readOnly = true)
    public UserEntity findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElse(null);
    }
}