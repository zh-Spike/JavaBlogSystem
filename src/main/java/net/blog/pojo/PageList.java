package net.blog.pojo;

import java.util.List;

public class PageList<T> {
    public PageList(long currentPage, long totalCount, long pageSize) {
        this.currentPage = currentPage;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.totalPage = this.totalCount / this.pageSize;
        // 是否第一页/最后一页
        // 第一页为0 最后一页为总的页码
        // 10, 一页有10个 == > 1
        // 100, 一页有10个 == > 10
        this.isFirst = this.currentPage == 1;
        this.isLast = this.currentPage == totalPage;
    }

    // 分页要做多少数据
    // 当前页码
    private long currentPage;
    // 总数量
    private long totalCount;
    // 每一页有多少数量
    private long pageSize;
    // 总页数
    private long totalPage;
    // 是否是第一页、最后一页
    private boolean isFirst;
    private boolean isLast;
    // 数据
    private List<T> contents;

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }
}
