package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.pojo.PageList;
import net.blog.pojo.SearchResult;
import net.blog.response.ResponseResult;
import net.blog.services.ISolrService;
import net.blog.utils.TextUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SolrServiceImpl extends BaseService implements ISolrService {

    @Autowired
    private SolrClient solrClient;

    @Override
    public ResponseResult doSearch(String keyword, int page, int size, String categoryId, Integer sort) {
        // 1. 检查page和size
        page = checkPage(page);
        size = checkSize(size);
        SolrQuery solrQuery = new SolrQuery();
        // 2. 分页设置
        // 先设置每页数量
        solrQuery.setRows(size);
        // 设置开始的位置
        // 第1页 从0开始
        // 第2页 -- >  size
        // 第3页 -- > 2 * size
        // 第n页 -- > (n-1) * size
        int start = (page - 1) * size;
        // solrQuery.set("start",start);
        solrQuery.setStart(start);
        // 3. 设置搜索条件
        // 关键字
        solrQuery.set("df", "search_item");
        // 条件过滤
        if (TextUtils.isEmpty(keyword)) {
            solrQuery.set("q", "*");
        } else {
            solrQuery.set("q", keyword);
        }
        // 排序: 1. 时间升序降序 2. 浏览量升序降序
        if (sort != null) {
            if (sort == 1) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.asc);
            } else if (sort == 2) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.desc);
            } else if (sort == 3) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.asc);
            } else if (sort == 4) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.desc);
            }
        }
        // 分类
        if (!TextUtils.isEmpty(categoryId)) {
            solrQuery.setFilterQueries("blog_category_id:" + categoryId);
        }
        // 关键字高亮
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("blog_title,blog_content");
        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        solrQuery.setHighlightFragsize(500);
        // 设置返回字段
        //blog_content,blog_create_time,blog_labels,blog_url,blog_title,blog_view_count
        solrQuery.addField("id,blog_content,blog_create_time,blog_labels,blog_url,blog_title,blog_view_count");
        // 4. 搜索
        try {
            // 4.1 处理搜索结果
            QueryResponse result = solrClient.query(solrQuery);
            // 获取到高亮信息
            Map<String, Map<String, List<String>>> highlighting = result.getHighlighting();
            // 把数据转成bean类
            List<SearchResult> resultList = result.getBeans(SearchResult.class);
            // 结果列表
            for (SearchResult item : resultList) {
                Map<String, List<String>> stringListMap = highlighting.get(item.getId());
                if (stringListMap != null) {
                    // 到下一个循环
                    continue;
                }
                List<String> blogContent = stringListMap.get("blog_content");
                if (blogContent != null) {
                    item.setBlogContent(blogContent.get(0));
                }
                List<String> blogTitle = stringListMap.get("blog_title");
                if (blogTitle != null) {
                    item.setBlogTitle(blogTitle.get(0));
                }
            }
            // 5. 返回搜索结果
            // 包含内容
            // 列表 页码 每页数量
            long numFound = result.getResults().getNumFound();
            PageList<SearchResult> pageList = new PageList<>(page,numFound,size);
            pageList.setContents(resultList);
            // 返回结果
            return ResponseResult.SUCCESS("搜索成功").setData(pageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("搜索失败，请稍后重试");
    }
}
