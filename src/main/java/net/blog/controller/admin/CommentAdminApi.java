package net.blog.controller.admin;

import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.handler.RequestMatchResult;

@RestController
@RequestMapping("/admin/comment")
public class CommentAdminApi {

    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId) {
        return null;
    }

    @GetMapping("/{commentId}")
    public ResponseResult getComment(@PathVariable("commentId") String commentId) {
        return null;
    }

    @GetMapping("/list")
    public ResponseResult listComments(@RequestParam("page") int page, @RequestParam("size") int size) {
        return null;
    }

    @PutMapping("/top/{commentId}")
    public RequestMatchResult topComment(@PathVariable("commentId") String commentId) {
        return null;
    }
}
