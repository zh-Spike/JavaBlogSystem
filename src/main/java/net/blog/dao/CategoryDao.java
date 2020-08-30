package net.blog.dao;

import net.blog.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category, String>, JpaSpecificationExecutor<Category> {
    Category findOneById(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_category SET state = 0 WHERE id = ? ")
    int deleteCategoriesByUpdateState(String categoryId);

    @Query(nativeQuery = true,value = "SELECT * FROM tb_category WHERE state = ? ORDER BY create_time DESC")
    List<Category> listCategoryByState(String status);
}
