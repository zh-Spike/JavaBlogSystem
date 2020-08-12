package net.blog.controller.admin;

import net.blog.response.ResponseResult;
import net.blog.services.IWebSiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/web_site_info")
public class WebSiteInfoAdminApi {

    @Autowired
    private IWebSiteInfoService webSiteInfoService;

    @GetMapping("/title")
    public ResponseResult getWebSiteTitle() {
        return webSiteInfoService.getWebsiteTitle();
    }

    @PutMapping("/title")
    public ResponseResult putWebSiteTitle(@RequestParam("title") String title) {
        return webSiteInfoService.putWebSiteTitle(title);
    }

    @GetMapping("/seo")
    public ResponseResult getSeoInfo() {
        return webSiteInfoService.getSeoInfo();
    }

    @PutMapping("/seo")
    public ResponseResult putSeoInfo(@RequestParam("keywords") String keywords,
                                     @RequestParam("description") String description) {
        return webSiteInfoService.putSeoInfo(keywords, description);
    }

    @GetMapping("/view_count")
    public ResponseResult getWebSiteViewCount() {
        return webSiteInfoService.getWebsiteViewCount();
    }

}
