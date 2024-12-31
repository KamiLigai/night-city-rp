package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDto<T> {

    private List<T> content;
    private Pageable pageable;
    private int size;
    private int totalElements;
    private int totalPages;
    private int number;
    private int numberOfElements;
    private boolean empty;
}
