package net.blog.dao;

import net.blog.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CategoryDao extends JpaRepository<Category, String>, JpaSpecificationExecutor<Category> {
    Category findOneById(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_category SET status = 0 WHERE id = ? ")
    int deleteCategoriesByUpdateState(String categoryId);
}
