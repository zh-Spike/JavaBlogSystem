package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.IArticleService;
import net.blog.services.ICategoryService;
import net.blog.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/article")
public class ArticlePortalApi {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IArticleService articleService;

    /**
     * 获取文章列表
     * 所有用户
     * 必须是已经发布的 置顶文章另一个接口
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticle(@PathVariable("page") int page, @PathVariable("size") int size) {
        return articleService.listArticles(page, size, null, null, Constants.Article.STATE_PUBLISH);
    }

    @GetMapping("/list/{categoryId}/{page}/{size}")
    public ResponseResult listArticleByCategoryId(@PathVariable("page") int page,
                                                  @PathVariable("size") int size,
                                                  @PathVariable("categoryId") String categoryId) {
        return articleService.listArticles(page, size, null, categoryId, Constants.Article.STATE_PUBLISH);
    }

    /**
     * 获取文章详情
     * 任意用户
     * <p>
     * 内容过滤 置顶的和已发布的
     * 其他的获取需要权限 草稿 --> 对应用户 已经删除的 --> admin
     *
     * @param articleId
     * @return
     */
    @GetMapping("/{articleId}")
    public ResponseResult getArticleDetail(@PathVariable("articleId") String articleId) {
        return articleService.getArticleById(articleId);
    }

    @GetMapping("/top")
    public ResponseResult getTopArticles() {
        return articleService.listTopArticles();
    }

    /**
     * 获取标签云 用户点击标签，通过标签获取相关文章列表
     * 任意用户
     *
     * @param size
     * @return
     */
    @GetMapping("/label/{size}")
    public ResponseResult getLabels(@PathVariable("size") int size) {
        return articleService.listLabels(size);
    }

    @GetMapping("/list/label/{label}/{page}/{size}")
    public ResponseResult listArticleByLabel(@PathVariable("page") int page,
                                             @PathVariable("size") int size,
                                             @PathVariable("label") String label) {
        return articleService.listArticlesByLabel(page, size, label);
    }

    /**
     * 通过标签来计算匹配度
     * 标签有一个或多个(<5)
     * 从里面随机拿出标签来 --> 每次获取的推荐文章不那么雷同,如果种子一样则相同
     * 通过标签去查询类似的文章,包含此标签的文章 如果没有从数据库中获取最新的文章
     *
     * @param articleId
     * @return
     */
    @GetMapping("/recommend/{articleId}/{size}")
    public ResponseResult getRecommendArticles(@PathVariable("articleId") String articleId,
                                               @PathVariable("size") int size) {
        return articleService.listRecommendArticle(articleId, size);
    }


    @GetMapping("/categories")
    public ResponseResult getCategories() {
        return categoryService.listCategories();
    }

}
