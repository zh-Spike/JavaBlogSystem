package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.ICategoryService;
import net.blog.services.IFriendLinkService;
import net.blog.services.ILooperService;
import net.blog.services.IWebSiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/web_site_info")
public class WebSiteInfoPortalApi {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IFriendLinkService friendLinkService;

    @Autowired
    private ILooperService looperService;

    @Autowired
    private IWebSiteInfoService webSiteInfoService;

    @GetMapping("/categories")
    public ResponseResult getCategories() {
        return categoryService.listCategories();
    }

    @GetMapping("/title")
    public ResponseResult getWebSiteTitle() {
        return webSiteInfoService.getWebsiteTitle();
    }

    @GetMapping("/view_count")
    public ResponseResult getWebSiteViewCount() {
        return webSiteInfoService.getWebsiteViewCount();
    }

    @GetMapping("/seo")
    public ResponseResult getSeo() {
        return webSiteInfoService.getSeoInfo();
    }

    @GetMapping("/loop")
    public ResponseResult getLoops() {
        return looperService.listLoops();
    }

    @GetMapping("/friend_link")
    public ResponseResult getFriendLink() {
        return friendLinkService.listFriendLinks();
    }
}
