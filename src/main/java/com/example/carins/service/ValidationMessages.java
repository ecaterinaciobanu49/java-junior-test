package com.example.carins.service;

public class ValidationMessages {

    public static final String INVALID_DATE_FORMAT = "The provided date format is invalid. Please use the ISO format: yyyy-MM-dd.";
    public static final String DATE_REQUIRED =
            "The date parameter is required and cannot be null.";
    public static final String DATE_TOO_EARLY =
            "The specified date is not valid because it is earlier than the supported range";
    public static final String DATE_TOO_LATE =
            "The specified date is not valid because it exceeds the maximum supported range";
    public static final String CAR_ID_REQUIRED =
            "The car id parameter is required and cannot be null.";
}
