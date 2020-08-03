package net.blog.dao;

import net.blog.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserDao extends  JpaSpecificationExecutor<User>,JpaRepository<User,String> {
}
