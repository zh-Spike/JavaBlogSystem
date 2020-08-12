package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/web_site_info")
public class WebSiteInfoPortalApi {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/categories")
    public ResponseResult getCategories() {
        return categoryService.listCategories();
    }

    @GetMapping("/title")
    public ResponseResult getWebSiteTitle() {
        return null;
    }

    @GetMapping("/view_count")
    public ResponseResult getWebSiteViewCount() {
        return null;
    }

    @GetMapping("/seo")
    public ResponseResult getSeo() {
        return null;
    }

    @GetMapping("/loop")
    public ResponseResult getLoops() {
        return null;
    }

    @GetMapping("/friend_link")
    public ResponseResult getFriendLink() {
        return null;
    }
}
