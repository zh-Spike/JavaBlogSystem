package net.blog.controller.admin;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.response.ResponseResult;
import net.blog.services.IWebSiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/web_site_info")
public class WebSiteInfoAdminApi {

    @Autowired
    private IWebSiteInfoService webSiteInfoService;

    @PreAuthorize("@permission.admin()")
    @GetMapping("/title")
    public ResponseResult getWebSiteTitle() {
        return webSiteInfoService.getWebsiteTitle();
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/title")
    public ResponseResult putWebSiteTitle(@RequestParam("title") String title) {
        return webSiteInfoService.putWebSiteTitle(title);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/seo")
    public ResponseResult getSeoInfo() {
        return webSiteInfoService.getSeoInfo();
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/seo")
    public ResponseResult putSeoInfo(@RequestParam("keywords") String keywords,
                                     @RequestParam("description") String description) {
        return webSiteInfoService.putSeoInfo(keywords, description);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/view_count")
    public ResponseResult getWebSiteViewCount() {
        return webSiteInfoService.getWebsiteViewCount();
    }

}
