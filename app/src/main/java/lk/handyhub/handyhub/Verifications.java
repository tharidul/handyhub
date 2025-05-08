package lk.handyhub.handyhub;

public class Verifications {
    public static String formatMobileNumber(String phoneNumber) {
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.trim();
            if (!phoneNumber.startsWith("+94")) {
                phoneNumber = "+94" + phoneNumber.replaceFirst("^0", "");
            }
        }
        return phoneNumber;
    }

    public static boolean isValidPassword(String password) {

        String passwordPattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$";
        return password.matches(passwordPattern);
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.trim();


            if (phoneNumber.startsWith("0")) {
                phoneNumber = formatMobileNumber(phoneNumber);
            }

            return phoneNumber.matches("\\+94\\d{9}");
        }
        return false;
    }


}
