package ai.datagym.application.utils;

import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public final class PageUtils {

    private PageUtils() {
    }

    // Use total elements for Paging
    public static CriteriaQuery<Long> getTotalElements(CriteriaBuilder criteriaBuilder, Class<?> clazz) {
        CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
        criteria.select(criteriaBuilder.count(criteria.from(clazz)));
        return criteria;
    }

    // Use amount of pages for Paging
    public static int getTotalPages(Long totalElements, int pageSize) {
        float totalPages = totalElements.floatValue() / (float) pageSize;
        return (int) Math.ceil(totalPages);
    }

    //return offset for paging
    public static int getOffset(Pageable pageable) {
        return pageable.getPageNumber() * pageable.getPageSize();
    }
}

