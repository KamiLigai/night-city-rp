package ru.nightcityroleplay.tests.util;

import lombok.experimental.UtilityClass;

import java.util.Base64;

@UtilityClass
public class HttpUtils {

    public static String getBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());
    }
}
