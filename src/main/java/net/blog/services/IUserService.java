package net.blog.services;

import net.blog.pojo.User;
import net.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService {

    ResponseResult initManagerAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletResponse response, String captchaKey)throws Exception;

    ResponseResult sendEmail(HttpServletRequest request, String emailAddress);
}
