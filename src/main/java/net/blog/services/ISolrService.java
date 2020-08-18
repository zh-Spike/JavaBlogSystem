package net.blog.services;

import net.blog.pojo.Article;
import net.blog.response.ResponseResult;

public interface ISolrService {
    ResponseResult doSearch(String keyword, int page, int size, String categoryId, Integer sort);

    void addArticle(Article article);

    void deleteArticle(String articleId);

    void updateArticle(String articleId, Article article);
}