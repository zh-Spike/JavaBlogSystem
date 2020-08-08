package net.blog.dao;

import net.blog.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserDao extends JpaSpecificationExecutor<User>, JpaRepository<User, String> {
    /**
     * 根据用户名查找
     *
     * @param userName
     * @return
     */
    User findOneByUserName(String userName);

    /**
     * 通过邮箱查找
     *
     * @param email
     * @return
     */
    User findOneByEmail(String email);

    /**
     * 通过UserId查找
     *
     * @param UserId
     * @return
     */
    User findOneById(String UserId);

}
