package net.blog.services.impl;

import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.blog.dao.ArticleDao;
import net.blog.pojo.Article;
import net.blog.utils.Constants;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class SolrTestService {

    @Autowired
    private SolrClient solrClient;

    public void add() {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "743100766356504576");
        doc.addField("blog_view_count", 5);
        doc.addField("blog_title", "Chuan");
        doc.addField("blog_content", "Changchun");
        doc.addField("blog_create_time", new Date());
        doc.addField("blog_labels", "ao-bao-aiaiai");
        doc.addField("blog_url", "https://chuancai.com");
        doc.addField("blog_category_id", "742403818976706560");
        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "743100766356504576");
        doc.addField("blog_view_count", 10);
        doc.addField("blog_title", "ChuanBao");
        doc.addField("blog_content", "Changchun123");
        doc.addField("blog_create_time", new Date());
        doc.addField("blog_labels", "ao-bao-aiaiai");
        doc.addField("blog_url", "https://chuancai.com");
        doc.addField("blog_category_id", "742403818976706560");
        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            // 删除一条的记录
            solrClient.deleteById("743100766356504576");

            // 删除所有
            // solrClient.deleteByQuery("*");
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private ArticleDao articleDao;

    public void importAll() {
        List<Article> all = articleDao.findAll();
        for (Article article : all) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", article.getId());
            doc.addField("blog_view_count", article.getViewCount());
            doc.addField("blog_title", article.getTitle());
            // 对内容进行处理 去标签 提取纯文本呢
            // 1. markdown type = 1
            // 2. 富文本 type = 0
            // 如果type == 1 == > 转换成html
            // 再从html == > 纯文本
            // 如果type == 0 == > 纯文本
            String type = article.getType();
            String html;
            if (Constants.Article.TYPE_MARKDOWN.equals(type)) {
                // 转成html
                // markdown to html
                MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        JekyllTagExtension.create(),
                        TocExtension.create(),
                        SimTocExtension.create()
                ));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(article.getContent());
                html = renderer.render(document);
            } else {
                html = article.getContent();
            }
            // 到这里不管是原来时什么内容 都变成html
            // html ==> text
            String content = Jsoup.parse(html).text();

            doc.addField("blog_content", content);
            doc.addField("blog_create_time", article.getCreateTime());
            doc.addField("blog_labels", article.getLabel());
            doc.addField("blog_url", "https://chuancai.com");
            doc.addField("blog_category_id", article.getCategoryId());
            try {
                solrClient.add(doc);
                solrClient.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteAll() {
        try {
            // 删除一条的记录
            // solrClient.deleteById("743100766356504576");
            // 删除所有
            solrClient.deleteByQuery("*");
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}