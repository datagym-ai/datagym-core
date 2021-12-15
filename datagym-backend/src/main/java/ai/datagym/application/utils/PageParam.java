package ai.datagym.application.utils;

/**
 * Paging information
 */
public class PageParam {
    private Integer pageIndex;
    private Integer numberOfElementsPerPage;
    private String searchString;

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getNumberOfElementsPerPage() {
        return numberOfElementsPerPage;
    }

    public void setNumberOfElementsPerPage(Integer numberOfElementsPerPage) {
        this.numberOfElementsPerPage = numberOfElementsPerPage;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
}
