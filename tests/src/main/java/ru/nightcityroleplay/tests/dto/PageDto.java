package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDto {

    List<Object> content;
    Pageable pageable;
    Object size;
    Object totalElements;
    Object totalPages;
    Object number;
    Object numberOfElements;
    Object empty;
}
