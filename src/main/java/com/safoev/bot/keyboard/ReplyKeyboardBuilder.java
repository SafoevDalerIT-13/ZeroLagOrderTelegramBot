package com.safoev.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReplyKeyboardBuilder {

    public ReplyKeyboardMarkup build(List<String> buttonTexts, int buttonsPerRow) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < buttonTexts.size(); i++) {
            currentRow.add(KeyboardButton.builder().text(buttonTexts.get(i)).build());

            if ((i + 1) % buttonsPerRow == 0 || i == buttonTexts.size() - 1) {
                rows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup createSimpleRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for (String button : buttons) {
            row.add(KeyboardButton.builder().text(button).build());
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup createTwoRows(String[] row1, String[] row2) {
        KeyboardRow firstRow = new KeyboardRow();
        for (String button : row1) {
            firstRow.add(KeyboardButton.builder().text(button).build());
        }

        KeyboardRow secondRow = new KeyboardRow();
        for (String button : row2) {
            secondRow.add(KeyboardButton.builder().text(button).build());
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(firstRow, secondRow))
                .resizeKeyboard(true)
                .build();
    }
}