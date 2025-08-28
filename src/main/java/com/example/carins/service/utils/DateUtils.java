package com.example.carins.service.utils;

import com.example.carins.service.ValidationMessages;

import java.time.LocalDate;

public class DateUtils {

    public static void checkDateErrors(LocalDate dateTocCheck) {
        if (dateTocCheck == null) {
            throw new IllegalArgumentException(ValidationMessages.DATE_REQUIRED);
        }
        if (dateTocCheck.isBefore(LocalDate.of(1950, 1, 1))) {
            throw new IllegalArgumentException(ValidationMessages.DATE_TOO_EARLY);
        }
        if (dateTocCheck.isAfter(LocalDate.now().plusYears(50))) {
            throw new IllegalArgumentException(ValidationMessages.DATE_TOO_LATE);
        }
    }
}
