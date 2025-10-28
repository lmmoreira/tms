package br.com.logistics.tms.commons.domain.pagination;

import br.com.logistics.tms.commons.domain.exception.DomainException;

public record PageRequest(int page, int size) {

    public PageRequest {
        if (page < 1) throw new DomainException("page must be >= 1");
        if (size < 1) throw new DomainException("size must be >= 1");
    }

    public int offset() {
        return (page - 1) * size;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

}
