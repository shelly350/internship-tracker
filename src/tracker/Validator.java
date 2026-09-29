package tracker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;


final class Validator {
    static final int MAX_COMPANY = 80;
    static final int MAX_ROLE = 100;
    static final int MAX_SUBJECT = 200;
    static final int MAX_NOTES = 500;

    private Validator() { }

    /** Trims the text and rejects control characters and over-long input. */
    static String text(String value, String field, int maxLength, boolean required) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is missing.");
        }
        String trimmed = value.trim();
        if (required && trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " is too long (max " + maxLength + " characters).");
        }
        for (int i = 0; i < trimmed.length(); i += 1) {
            if (Character.isISOControl(trimmed.charAt(i))) {
                throw new IllegalArgumentException(
                        field + " contains a control character.");
            }
        }
        return trimmed;
    }

    /** Parses a date written like 2026-09-28. */
    static LocalDate date(String value, String field) {
        try {
            LocalDate d = LocalDate.parse(value.trim());
            if (d.getYear() < 2000) {
                throw new IllegalArgumentException(field + " is too far in the past.");
            }
            return d;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(field + " must look like 2026-09-28.");
        }
    }

    /** Rejects dates after today. */
    static LocalDate notInFuture(LocalDate d, String field) {
        if (d.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(field + " cannot be in the future.");
        }
        return d;
    }
}
