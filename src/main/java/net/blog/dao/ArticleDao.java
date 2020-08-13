package net.blog.dao;

import net.blog.pojo.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleDao extends JpaRepository<Article, String>, JpaSpecificationExecutor<Article> {
    Article findOneById(String id);

    @Modifying
    int deleteAllById(String articleId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_article SET state = '0' WHERE id = ? ")
    int deleteArticleByState(String articleId);

    @Query(nativeQuery = true,value ="SELECT labels from tb_article where id = ?")
    String listArticleLabelsById(String articleId);
}
