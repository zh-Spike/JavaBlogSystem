package net.blog.dao;

import net.blog.pojo.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
     * @param userId
     * @return
     */
    User findOneById(String userId);

    /**
     * 修改用户状态
     *
     * @param userId
     * @return
     */
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_user SET state = '0' WHERE id = ? ")
    int deleteUserByState(String userId);


    @Query(value = "select new User(u.id,u.userName,u.roles,u.avatar,u.email,u.sign,u.state,u.regIp,u.loginIp,u.createTime,u.updateTime) from User as u")
    Page<User> listAllUserNoPassword(Pageable pageable);


    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_user SET password = ? WHERE email = ? ")
    int updatePasswordByEmail(String encode, String email);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_user SET email = ? WHERE id = ? ")
    int updateEmailById(String email, String id);
}
