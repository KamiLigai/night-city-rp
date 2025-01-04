package ru.nightcityroleplay.tests.dto;

public record HttpResponse(
    int code,
    String body
) {
}
