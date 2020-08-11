package net.blog.services;

import net.blog.pojo.FriendLink;
import net.blog.response.ResponseResult;

public interface IFriendLinkService {
    ResponseResult addFriendLink(FriendLink friendLink);

    ResponseResult getFriendLink(String friendLinkId);

    ResponseResult listFriendLinks(int page, int size);

    ResponseResult deleteFriendLink(String friendLinkId);

    ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink);
}
