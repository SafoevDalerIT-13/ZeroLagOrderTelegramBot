package com.safoev.bot.handler;

import com.safoev.bot.keyboard.KeyboardFactory;
import com.safoev.bot.menu.MenuService;
import com.safoev.user.domain.dto.request.UserRegisterRequestDto;
import com.safoev.user.domain.dto.response.UserRegisterResponseDto;
import com.safoev.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationHandler {

    private final MessageSender messageSender;
    private final KeyboardFactory keyboardFactory;
    private final MenuService menuService;
    private final UserService userService;

    private final Map<Long, String> regStep = new ConcurrentHashMap<>();
    private final Map<Long, UserRegisterRequestDto> regData = new ConcurrentHashMap<>();

    public void start(Long chatId, User telegramUser) {
        if (userService.existsByTelegramId(chatId)) {
            // Если уже зарегистрирован - показываем меню
            menuService.sendMainMenu(chatId, telegramUser);
            return;
        }

        regStep.put(chatId, "ENTER_FIRST_NAME");
        UserRegisterRequestDto dto = new UserRegisterRequestDto();
        dto.setTelegramUsername(telegramUser.getUserName());
        regData.put(chatId, dto);

        String welcomeMessage = """
                📝 Давайте зарегистрируемся!
                
                Я буду задавать вам вопросы по очереди.
                Для отмены нажмите "❌ Отмена".
                
                👤 Введите ваше имя:
                """;

        messageSender.sendWithKeyboard(
                chatId,
                welcomeMessage,
                keyboardFactory.createCancelKeyboard()
        );
    }

    public void handleInput(Long chatId, String input, User telegramUser) {
        if (input.equals("❌ Отмена")) {
            cancel(chatId);
            return;
        }

        String step = regStep.get(chatId);
        UserRegisterRequestDto dto = regData.get(chatId);

        try {
            switch (step) {
                case "ENTER_FIRST_NAME":
                    dto.setFirstName(input);
                    regStep.put(chatId, "ENTER_LAST_NAME");
                    messageSender.send(chatId, "Введите фамилию (или '-' чтобы пропустить):");
                    break;

                case "ENTER_LAST_NAME":
                    if (!input.equals("-")) {
                        dto.setLastName(input);
                    }
                    regStep.put(chatId, "ENTER_PHONE");
                    messageSender.send(chatId, "📱 Введите номер телефона (например: +79991234567):");
                    break;

                case "ENTER_PHONE":
                    if (!input.matches("^\\+?[0-9]{10,15}$")) {
                        messageSender.send(chatId, "❌ Неверный формат. Попробуйте еще раз:");
                        return;
                    }
                    dto.setPhone(input);
                    complete(chatId);
                    break;

                default:
                    cancel(chatId);
            }
        } catch (Exception e) {
            log.error("Ошибка регистрации: {}", e.getMessage());
            messageSender.send(chatId, "Произошла ошибка. Попробуйте еще раз.");
            cancel(chatId);
        }
    }

    private void complete(Long chatId) {
        try {
            UserRegisterRequestDto dto = regData.get(chatId);
            UserRegisterResponseDto response = userService.registerUser(chatId, dto);

            regStep.remove(chatId);
            regData.remove(chatId);

            String successMessage = String.format("""
                🎉 Регистрация успешно завершена!
                
                Добро пожаловать, %s!
                
                Выберите действие:
                """, response.getFirstName());

            // После регистрации показываем приветствие (один раз)
            menuService.sendAuthorizedMenu(chatId, successMessage);

        } catch (Exception e) {
            log.error("Ошибка завершения регистрации: {}", e.getMessage());
            messageSender.send(chatId, "❌ Ошибка: " + e.getMessage());
            cancel(chatId);
        }
    }

    public void cancel(Long chatId) {
        regStep.remove(chatId);
        regData.remove(chatId);
        messageSender.send(chatId, "❌ Регистрация отменена.");
        menuService.sendMainMenu(chatId, null);
    }

    public boolean isInProgress(Long chatId) {
        return regStep.containsKey(chatId);
    }
}