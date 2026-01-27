package com.etz.Utils;

public class Validation {

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    private static boolean isValidPhone(String phone_number) {
        String phoneRegex = "^[0-9]{10}$";
        return phone_number != null && phone_number.matches(phoneRegex);
    }

    private static boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_-]{4,16}$";
        return username != null && username.matches(usernameRegex);
    }

    private static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && (accountNumber.matches("CUR-\\d{6}") || accountNumber.matches("SAV-\\d{6}"));
    }

    private static boolean isValidPin(String pin) {
        return pin != null && pin.length() == 4 && pin.matches("\\d+");
    }


    // Accept the raw input as a String, ensure it's numeric (integer or decimal), parse and check > 0
    private static boolean isValidAmount(String amountStr) {
        if (amountStr == null) {
            return false;
        }
        // allow integers or decimals like "100" or "100.50"
        if (!amountStr.matches("^\\d+(\\.\\d+)?$")) {
            return false;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValid(String field, String value) {
        return switch (field) {
            case "email" -> isValidEmail(value);
            case "phone_number" -> isValidPhone(value);
            case "username" -> isValidUsername(value);
            case "password" -> isValidPassword(value);
            case "accountNumber" -> isValidAccountNumber(value);
            case "pin" -> isValidPin(value);
            case "amount" -> isValidAmount(value);
            default -> false;
        };
    }


    /*      Ensures the string is not null.
            Must contain at least one digit.
            Must contain at least one lowercase letter.
            Must contain at least one uppercase letter.
            Must contain at least one special character from the set @#$%^&+=.
            Must not contain any whitespace characters.
            Must be at least 8 characters long.*/
    private static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password != null && password.matches(passwordRegex);
    }
}
