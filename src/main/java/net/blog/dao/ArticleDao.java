package net.blog.dao;

import net.blog.pojo.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArticleDao extends JpaRepository<Article,String>, JpaSpecificationExecutor<Article> {
    Article findOneById(String id);
}
