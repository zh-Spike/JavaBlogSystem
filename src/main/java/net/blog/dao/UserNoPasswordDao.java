package net.blog.dao;

import net.blog.pojo.UserNoPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserNoPasswordDao extends JpaSpecificationExecutor<UserNoPassword>, JpaRepository<UserNoPassword, String> {
}
