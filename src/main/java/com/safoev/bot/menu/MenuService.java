package com.safoev.bot.menu;

import com.safoev.bot.handler.MessageSender;
import com.safoev.bot.keyboard.KeyboardFactory;
import com.safoev.user.domain.db.UserEntity;
import com.safoev.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MessageSender messageSender;
    private final KeyboardFactory keyboardFactory;
    private final UserService userService;

    // Этот метод вызывается только при /start
    public void sendMainMenu(Long chatId, User telegramUser) {
        if (userService.existsByTelegramId(chatId)) {
            // Пользователь зарегистрирован - показываем приветствие с именем (только при /start)
            UserEntity user = userService.findByTelegramId(chatId);
            String greeting = String.format("С возвращением, %s! 👋\n\nВыберите действие:",
                    user.getFirstName());
            sendAuthorizedMenu(chatId, greeting);
        } else {
            // Пользователь не зарегистрирован
            sendUnauthorizedMenu(chatId);
        }
    }

    // Этот метод для обычных переходов по меню (без приветствия)
    public void sendAuthorizedMenu(Long chatId) {
        messageSender.sendWithKeyboard(
                chatId,
                "Выберите действие:",
                keyboardFactory.createMainMenuForAuthorized()
        );
    }

    // Перегруженный метод для случаев, когда нужно свое сообщение
    public void sendAuthorizedMenu(Long chatId, String greeting) {
        messageSender.sendWithKeyboard(
                chatId,
                greeting,
                keyboardFactory.createMainMenuForAuthorized()
        );
    }

    public void sendUnauthorizedMenu(Long chatId) {
        String greeting = """
                Привет! Я бот для создания заказов! 🤖
                
                Вы можете создать заказ без регистрации или зарегистрироваться для отслеживания истории заказов.
                """;

        messageSender.sendWithKeyboard(
                chatId,
                greeting,
                keyboardFactory.createMainMenuForUnauthorized()
        );
    }

    public void sendHelp(Long chatId) {
        String help = """
                ℹ️ Помощь по боту
                
                📦 Создание заказа:
                • Нажмите "Создать заказ" в меню
                • Следуйте инструкциям
                
                📋 Мои заказы:
                • Просмотр истории заказов
                
                👤 Профиль:
                • Просмотр личных данных
                
                /start - Главное меню
                """;

        messageSender.sendWithKeyboard(
                chatId,
                help,
                keyboardFactory.createBackToMenuKeyboard()
        );
    }

    public void sendUnknownCommand(Long chatId) {
        String message = """
                Я вас пока не понимаю! 🤔
                
                Доступные команды:
                /start - Начать работу
                /help - Помощь
                """;
        messageSender.send(chatId, message);
    }
    public void sendMessage(Long chatId, String text) {
        messageSender.send(chatId, text);
    }
}