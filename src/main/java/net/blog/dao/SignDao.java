package net.blog.dao;

import net.blog.pojo.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SignDao extends JpaRepository<Sign, String>, JpaSpecificationExecutor<Sign> {
    Sign findOneById(String id);
}
