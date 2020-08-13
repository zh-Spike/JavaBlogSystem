package net.blog.services;

import net.blog.pojo.Comment;
import net.blog.response.ResponseResult;

public interface ICommentService {
    ResponseResult postComment(Comment comment);

    ResponseResult listCommentByArticleId(String commentId, int page, int size);

    ResponseResult deleteCommentById(String commentId);
}
