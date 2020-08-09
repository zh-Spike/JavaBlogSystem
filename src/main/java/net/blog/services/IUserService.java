package net.blog.services;

import net.blog.pojo.User;
import net.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService {

    ResponseResult initManagerAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception;

    ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress);

    ResponseResult register(User user, String emailCode, String captchaCode,
                            String captchaKey, HttpServletRequest request);

    ResponseResult doLogin(String captcha, String captchaKey,
                           User user, HttpServletRequest request,
                           HttpServletResponse response);

    User checkUser(HttpServletRequest request, HttpServletResponse response);

    ResponseResult getUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String userId, User user);

    ResponseResult deleteUserById(String userId, HttpServletRequest request, HttpServletResponse response);

    ResponseResult listUsers(int page, int size, HttpServletRequest request, HttpServletResponse response);
}
