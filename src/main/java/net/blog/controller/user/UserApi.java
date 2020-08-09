package net.blog.controller.user;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @PostMapping("/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha") String captcha,
                                @PathVariable("captcha_key") String captchaKey,
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
     * <p>
     * 允许用户修改的内容
     * 1.头像
     * 2.用户名 唯一
     * 3.签名
     * 4.密码 单独
     * 5.Email 唯一单独
     *
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 修改信息user-info
     *
     * @return
     */
    @PutMapping("/{userId}")
    public ResponseResult updateUserInfo(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @PathVariable("userId") String userId,
                                         @RequestBody User user) {
        return userService.updateUserInfo(request, response, userId, user);
    }

    /**
     * 获取用户列表
     * 权限：admin
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    public ResponseResult listUser(@RequestParam("page") int page,
                                   @RequestParam("size") int size,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        return userService.listUsers(page, size, request, response);
    }

    /**
     * 管理员权限
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("userId") String userId) {
        // 判断当前用户
        // 根据当前用户角色来删除
        // 通过注解来控制权限
        return userService.deleteUserById(userId, request, response);
    }

    /**
     * 检查Email是否已经注册
     *
     * @param email
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "当前邮箱已注册"),
            @ApiResponse(code = 40000, message = "当前邮箱未注册")
    })
    @GetMapping("/email")
    public ResponseResult checkEmail(@RequestParam("email") String email) {
        return userService.checkEmail(email);
    }


    /**
     * 检查Email是否已经注册
     *
     * @param userName
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "当前用户名已注册"),
            @ApiResponse(code = 40000, message = "当前用户名未注册")
    })
    @GetMapping("/user_name")
    public ResponseResult checkUserName(@RequestParam("userName") String userName) {
        return userService.checkUserName(userName);
    }
}

