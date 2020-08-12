package net.blog.services;

import net.blog.pojo.Article;
import net.blog.response.ResponseResult;

public interface IArticleService {
    ResponseResult postArticle(Article article);

    ResponseResult listArticle(int page, int size, String state,
                               String keyword, String categoryId);

}
