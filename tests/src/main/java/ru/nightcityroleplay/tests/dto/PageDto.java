package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDto {
    private List<Object> content;
    private Pageable pageable;
    private Object size;
    private Object totalElements;
    private Object totalPages;
    private Object number;
    private Object numberOfElements;
    private Object empty;
}
