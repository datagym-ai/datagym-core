package ai.datagym.application.utils;

import java.util.List;

public class PageReturn<T> {
    private List<T> elements;

    private int totalPages;
    private Long totalElements;

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public List<T> getElements() {
        return elements;
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "PageReturnTO{" +
                "elements=" + elements +
                ", totalPages=" + totalPages +
                ", totalElements=" + totalElements +
                '}';
    }
}
