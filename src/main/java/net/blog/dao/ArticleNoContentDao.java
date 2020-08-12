package net.blog.dao;

import net.blog.pojo.Article;
import net.blog.pojo.ArticleNoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArticleNoContentDao extends JpaRepository<ArticleNoContent,String>, JpaSpecificationExecutor<ArticleNoContent> {
    Article findOneById(String id);
}
