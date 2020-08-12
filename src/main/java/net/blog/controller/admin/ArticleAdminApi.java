package net.blog.controller.admin;

import net.blog.pojo.Article;
import net.blog.response.ResponseResult;
import net.blog.services.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
public class ArticleAdminApi {

    @Autowired
    private IArticleService articleService;

    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult postArticle(@RequestBody Article article) {
        return articleService.postArticle(article);
    }

    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId") String articleId) {
        return null;
    }

    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId") String articleId) {
        return null;
    }

    @GetMapping("/{articleId}")
    public ResponseResult getArticle(@PathVariable("articleId") String articleId) {
        return null;
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page") int page,
                                       @PathVariable("size") int size,
                                       @RequestParam(value = "state", required = false) String state,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "categoryId", required = false) String categoryId) {
        return articleService.listArticle(page, size, state, keyword, categoryId);
    }

    @PutMapping("/sate/{articleId}/{state}")
    public ResponseResult updateArticleState(@PathVariable("articleId") String articleId, @PathVariable("state") String state) {
        return null;
    }

    @PutMapping("/top/{articleId}")
    public ResponseResult updateArticleState(@PathVariable("articleId") String articleId) {
        return null;
    }
}
