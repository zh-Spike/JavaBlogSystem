package net.blog.dao;

import net.blog.pojo.FriendLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FriendLinkDao extends JpaSpecificationExecutor<FriendLink>, JpaRepository<FriendLink, String> {
    FriendLink findOneById(String id);

    int deleteAllById(String friendLinkId);

}
