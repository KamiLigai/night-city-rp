package ru.nightcityroleplay.tests.constants;

import okhttp3.MediaType;

public class HttpConstants {

    public static class MediaTypes {
        public static final MediaType APP_JSON = MediaType.parse("application/json");
    }

    public static class Headers {
        public static final String AUTHORIZATION = "Authorization";
    }
}
