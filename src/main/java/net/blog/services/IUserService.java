package net.blog.services;

import net.blog.pojo.User;
import net.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService {

    ResponseResult initManagerAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletResponse response) throws Exception;

    ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress, String captchaCode);

    ResponseResult register(User user, String emailCode, String captchaCode);

    ResponseResult doLogin(String captcha, User user, String from);

    User checkUser();

    ResponseResult getUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(String userId, User user);

    ResponseResult deleteUserById(String userId);

    ResponseResult listUsers(int page, int size, String userName, String email);

    ResponseResult updateUserPassword(String verifyCode, User user);

    ResponseResult updateEmail(String email, String verifyCode);

    ResponseResult doLogout();

    ResponseResult getPLoginQrCodeInfo();

    ResponseResult checkQrCodeLoginState(String loginId);

    ResponseResult updateQrCodeLoginState(String loginId);

    ResponseResult parseToken();

    ResponseResult resetPassword(String userId, String password);

    ResponseResult getRegisterCount();

    ResponseResult checkEmailCode(String email, String emailCode, String captchaCode);
}
