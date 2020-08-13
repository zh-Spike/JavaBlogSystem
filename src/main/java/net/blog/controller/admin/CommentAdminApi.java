package net.blog.controller.admin;

import net.blog.response.ResponseResult;
import net.blog.services.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
public class CommentAdminApi {

    @Autowired
    private ICommentService commentService;

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId) {
        return commentService.deleteCommentById(commentId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listComments(@RequestParam("page") int page, @RequestParam("size") int size) {
        return commentService.listComments(page,size);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/top/{commentId}")
    public ResponseResult topComment(@PathVariable("commentId") String commentId) {
        return commentService.topComment(commentId);
    }
}
