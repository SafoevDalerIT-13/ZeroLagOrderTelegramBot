package com.safoev.bot;

import com.safoev.bot.handler.CallbackHandler;
import com.safoev.bot.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage()) {
                messageHandler.handle(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                callbackHandler.handle(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Ошибка обработки: {}", e.getMessage(), e);
        }
    }
}