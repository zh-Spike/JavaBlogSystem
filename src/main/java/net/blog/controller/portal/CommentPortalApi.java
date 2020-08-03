package net.blog.controller.portal;

import net.blog.pojo.Comment;
import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/comment")
public class CommentPortalApi {

    @PostMapping
    public ResponseResult postComment(@RequestBody Comment comment){
        return null;
    }

    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId){
        return null;
    }

    @GetMapping("/list/{commentId}")
    public ResponseResult listComments(@PathVariable("commentId") String commentId)
    {
        return null;

    }
}
