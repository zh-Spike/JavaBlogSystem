package net.blog.dao;

import net.blog.pojo.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RefreshTokenDao extends JpaRepository<RefreshToken,String>, JpaSpecificationExecutor<RefreshToken> {
}
