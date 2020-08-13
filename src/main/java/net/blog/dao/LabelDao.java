package net.blog.dao;

import net.blog.pojo.Labels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LabelDao extends JpaRepository<Labels, String>, JpaSpecificationExecutor<Labels> {

    @Modifying
    int deleteOneById(String id);

    /**
     * 根据ID查找标签
     *
     * @param id
     * @return
     */
    Labels findOneById(String id);

    Labels findOneByName(String name);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `tb_labels` SET `count` = `count` + 1 WHERE `name` = ?")
    int updateCountByName(String labelName);
}
