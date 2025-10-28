package br.com.logistics.tms.commons.domain.pagination;

import br.com.logistics.tms.commons.domain.exception.DomainException;

import java.util.List;

public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {

    public Page {
        if (content == null) throw new DomainException("content cannot be null");
        if (page < 1) throw new DomainException("page must be >= 1");
        if (size < 1) throw new DomainException("size must be >= 1");
        if (totalElements < 0) throw new DomainException("totalElements cannot be negative");
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    public boolean hasPrevious() {
        return page > 1;
    }
}
