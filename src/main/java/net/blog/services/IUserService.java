package net.blog.services;

import net.blog.pojo.User;
import net.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;

public interface IUserService {

    ResponseResult initManagerAccount(User user, HttpServletRequest request);
}
