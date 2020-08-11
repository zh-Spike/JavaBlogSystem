package net.blog.controller.admin;

import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/web_site_info")
public class WebSiteInfoAdminApi {

    @GetMapping("/title")
    public ResponseResult getWebSiteTitle() {
        return null;
    }

    @PutMapping("/title")
    public ResponseResult upWebSiteTitle(@RequestParam("title") String title) {
        return null;
    }

    @GetMapping("/seo")
    public ResponseResult getSeoInfo() {
        return null;
    }

    @PutMapping("/seo")
    public ResponseResult putSeoInfo(@RequestParam("keywords") String keywords,
                                     @RequestParam("description") String description) {
        return null;
    }

    @GetMapping("/view_count")
    public ResponseResult getWebSiteViewCount() {
        return null;
    }

}
