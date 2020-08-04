package net.blog.dao;

import net.blog.pojo.Labels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;

public interface LabelDao extends JpaRepository<Labels,String>, JpaSpecificationExecutor<Labels> {

    @Modifying
    int deleteOneById(String id);

    /**
     * 根据ID查找标签
     *
     * @param id
     * @return
     */
    Labels findOneById(String id);
}
