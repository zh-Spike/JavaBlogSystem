package net.blog.dao;

import net.blog.pojo.FriendLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FriendLinkDao extends JpaSpecificationExecutor<FriendLink>, JpaRepository<FriendLink, String> {
    FriendLink findOneById(String id);

    int deleteAllById(String friendLinkId);

    @Query(nativeQuery = true,value = "SELECT * FROM tb_friend_link WHERE state = ? ORDER BY create_time DESC")
    List<FriendLink> listFriendLink(String state);
}
