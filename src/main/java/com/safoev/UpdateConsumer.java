package com.safoev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public UpdateConsumer(@Value("${telegram.bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        if(update.hasMessage()) {
            String messageText = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            if(messageText.equals("/start")) {
                try {
                    sendMainMenu(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                SendMessage message = SendMessage.builder()
                        .text("Я вас ПОКА не понимаю!")
                        .chatId(chatId)
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        switch (data) {
            case "registration" -> buttonOne(chatId);
            case "non_registration" -> buttonTwo(chatId);
            default -> sendMessage(chatId,"Неизвестная команда");
        }
    }

    private void sendMessage(Long chatId, String s) {

    }

    private void buttonTwo(Long chatId) {

    }

    private void buttonOne(Long chatId) {

    }

    private void sendMainMenu(Long chatId) throws TelegramApiException{
        SendMessage message = SendMessage.builder()
                .text("Привет! Я бот для создания твоих заказов! Выбери действие: ")
                .chatId(chatId)
                .build();

        var button1 = InlineKeyboardButton.builder()
                .text("Зарегистрироваться")
                .callbackData("registration")
                .build();

        var button2 = InlineKeyboardButton.builder()
                .text("Продолжить без регистрации")
                .callbackData("non_registration")
                .build();

        List<InlineKeyboardRow> inlineKeyboardRowList = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(inlineKeyboardRowList);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }
}