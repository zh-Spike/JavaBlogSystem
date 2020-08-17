package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.ISolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/search")
public class SearchPortalApi {

    @Autowired
    private ISolrService solrService;

    @GetMapping
    public ResponseResult doSearch(@RequestParam("keyword") String keyword,
                                   @RequestParam("page") int page,
                                   @RequestParam("size") int size,
                                   @RequestParam(value = "categoryId", required = false) String categoryId,
                                   @RequestParam(value = "sort", required = false) Integer sort) {
        return solrService.doSearch(keyword, page, size, categoryId, sort);
    }
}
