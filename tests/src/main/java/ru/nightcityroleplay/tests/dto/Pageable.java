package ru.nightcityroleplay.tests.dto;

import lombok.Data;

@Data
public class Pageable {

    private int pageSize;
    private int offset;
    private int totalElements;
    private int totalPages;
    private int totalElementsPerPage;
    private int first;
    private int last;
}
