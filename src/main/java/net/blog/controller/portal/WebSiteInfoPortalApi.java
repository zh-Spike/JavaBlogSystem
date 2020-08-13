package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.ICategoryService;
import net.blog.services.IFriendLinkService;
import net.blog.services.ILooperService;
import net.blog.services.IWebSiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * 1. 痛统计访问页,没个页面算一次 PV/page/view
     * 直接增加访问量
     * 2. 通过ip进行过滤,借用第三方的工具
     * 递增
     * 统计信息,通过redis统计,数据也保存在mysql里
     * 不能每次都更新mysql 当用户去获取访问量时 更新一次
     * <p>
     * redis时机: 每个页面访问时,如果不是从mysql里读,写道redis
     * 如果是 自增
     * <p>
     * mysql时机: 用户访问网站总访问量时,从redis读取并更新到mysql中
     * 如果redis里没有 就读取mysql到redis
     */
    @PutMapping("/view_count")
    public void updateViewCount() {
        webSiteInfoService.updateViewCount();
    }
}
