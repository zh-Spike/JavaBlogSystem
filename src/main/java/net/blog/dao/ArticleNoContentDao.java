package net.blog.dao;

import net.blog.pojo.ArticleNoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleNoContentDao extends JpaRepository<ArticleNoContent, String>, JpaSpecificationExecutor<ArticleNoContent> {
    ArticleNoContent findOneById(String id);

    @Query(nativeQuery = true, value = "SELECT * FROM `tb_article` WHERE `labels` LIKE ? AND `id` != ? AND (`state` = '1' OR `state` = '3') LIMIT ?")
    List<ArticleNoContent> listArticleByLikeLabel(String label, String originalArticleId, int size);

    @Query(nativeQuery = true, value = "SELECT * FROM `tb_article` WHERE `id` != ? AND (`state` = '1' OR `state` = '3') ORDER BY `create_time` DESC LIMIT ?")
    List<ArticleNoContent> listLastArticleBySize(String originalArticleId, int size);
}
