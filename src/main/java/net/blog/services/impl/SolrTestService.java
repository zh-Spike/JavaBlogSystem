package net.blog.services.impl;

import net.blog.dao.ArticleDao;
import net.blog.pojo.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolrTestService {

    @Autowired
    private SolrC
    public void add(){

    }
    public void update(){

    }

    public void delete(){

    }

    @Autowired
    private ArticleDao articleDao;

    public void importALl(){
        List<Article> all = articleDao.findAll();
        for(Article article : all){
            SolrInputDocument doc = new SolrInputDocument();
            doc.add

        }
    }
}
