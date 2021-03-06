package net.blog.services;

import net.blog.pojo.Sign;
import net.blog.response.ResponseResult;

public interface ISignService {
    ResponseResult signIn(Sign sign);

    ResponseResult signOut(String signId);

    ResponseResult updateSign(String signId);

    ResponseResult listSigns();

    ResponseResult listUserSign();
}
