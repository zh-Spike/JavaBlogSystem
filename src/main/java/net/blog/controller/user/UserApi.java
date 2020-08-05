package net.blog.controller.user;

import lombok.extern.slf4j.Slf4j;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private IUserService userService;

    /**
     * 初始化管理员账号 init-admin
     */
    @PostMapping("/admin_account")
    public ResponseResult initManagerAccount(@RequestBody User user, HttpServletRequest request) {
        log.info("user name ==> " + user.getUserName());
        log.info("password ==> " + user.getPassword());
        log.info("email ==> " + user.getEmail());
        return userService.initManagerAccount(user, request);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ResponseResult register(@RequestBody User user,
                                   @RequestParam("email_code") String emailCode,
                                   @RequestParam("captcha_code") String captchaCode,
                                   @RequestParam("captcha_key") String captchaKey,
                                   HttpServletRequest request) {
        return userService.register(user, emailCode, captchaCode, captchaKey, request);
    }

    /**
     * 登录sign-up
     * 需要提交的数据
     * 1.账号、邮箱 唯一
     * 2.密码
     * 3.图灵验证码
     * 4.图灵验证码key
     *
     * @param captcha
     * @param user
     * @param captchaKey
     * @return
     */
    @PostMapping("/{captcha}")
    public ResponseResult login(@PathVariable("captcha_key") String captchaKey,
                                @PathVariable("captcha") String captcha,
                                @RequestBody User user,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        return userService.doLogin(captcha, captchaKey, user, request, response);
    }

    /**
     * 验证码
     * 有效时长10mins
     *
     * @return
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, @RequestParam("captcha_key") String captchaKey) {
        try {
            userService.createCaptcha(response, captchaKey);
        } catch (Exception e) {
            log.error(response.toString());
        }
    }

    /**
     * 发邮件
     *
     * @return
     */
    @GetMapping("/verify_code")
    public ResponseResult sendVerifyCode(HttpServletRequest request, @RequestParam("type") String type,
                                         @RequestParam("email") String emailAddress) {
        log.info("email ==>" + emailAddress);
        return userService.sendEmail(type, request, emailAddress);
    }

    /**
     * 修改密码 UpdatePassword
     *
     * @return
     */

    @PutMapping("/password")
    public ResponseResult updatePassword(@RequestBody User user) {
        return null;
    }

    /**
     * 作者信息user-info
     *
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId) {
        return null;
    }

    /**
     * 修改信息user-info
     *
     * @return
     */
    @PutMapping
    public ResponseResult updateUserInfo(@RequestBody User user) {
        return null;
    }

    @GetMapping("/list")
    public ResponseResult listUser(@RequestParam("page") int page, @RequestParam("size") int size) {
        return null;
    }

    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId) {
        return null;
    }

}



