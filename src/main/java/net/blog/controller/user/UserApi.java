package net.blog.controller.user;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PostMapping("/sign_up")
    public ResponseResult register(@RequestBody User user,
                                   @RequestParam("email_code") String emailCode,
                                   @RequestParam("captcha_code") String captchaCode,
                                   @RequestParam("captcha_key") String captchaKey,
                                   HttpServletRequest request) {
        return userService.register(user, emailCode, captchaCode, captchaKey, request);
    }

    /**
     * 登录
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
    @PostMapping("/login/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha") String captcha,
                                @PathVariable("captcha_key") String captchaKey,
                                @RequestBody User user,
                                @RequestParam(value = "from", required = false) String from) {
        return userService.doLogin(captcha, captchaKey, user, from);
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
     * 修改密码，找回密码
     * 普通：旧密码对比来更新密码
     * 找回：发送验证码到邮箱或手机，判断对应验证码是否正确 判断主人
     * <p>
     * 1. 获取邮箱
     * 2. 用户获取验证码
     * 3. 填写验证码
     * 4. 填写新的密码
     * 5. 提交数据 邮箱、新密码、验证码 type=forget
     * <p>
     * 验证码正确-->该账号是你的,可以修改密码
     *
     * @return
     */

    @PutMapping("/password/{verify_code}")
    public ResponseResult updatePassword(@PathVariable("verify_code") String verify_code,
                                         @RequestBody User user) {
        return userService.updateUserPassword(verify_code, user);
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
    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 修改信息user-info
     *
     * @return
     */
    @PutMapping("/user_info/{userId}")
    public ResponseResult updateUserInfo(@PathVariable("userId") String userId,
                                         @RequestBody User user) {
        return userService.updateUserInfo(userId, user);
    }

    /**
     * 获取用户列表
     * 权限：admin
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listUser(@RequestParam("page") int page,
                                   @RequestParam("size") int size,
                                   @RequestParam(value = "userName", required = false) String userName,
                                   @RequestParam(value = "email", required = false) String email) {
        return userService.listUsers(page, size, userName, email);
    }

    /**
     * 管理员权限
     *
     * @param userId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId) {
        // 判断当前用户
        // 根据当前用户角色来删除
        // 通过注解来控制权限
        return userService.deleteUserById(userId);
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

    /**
     * 1. 必须登录
     * 2. 新邮箱未注册
     * <p>
     * 用户
     * 1. 已经登陆
     * 2. 输入新的邮箱地址
     * 3. 获取验证码 type=update
     * 4. 输入验证码
     * 5. 提交数据
     * <p>
     * 1. 新邮箱地址
     * 2. 新验证码
     * 3. 其他从token读取
     *
     * @return
     */
    @PutMapping("/email")
    public ResponseResult updateEmail(@RequestParam("email") String email,
                                      @RequestParam("verify_code") String verifyCode) {
        return userService.updateEmail(email, verifyCode);
    }

    /**
     * 退出登录
     * <p>
     * 拿到token_key
     * -> 删除redis里对应的token
     * -> 删除mysql里对应的refreshToken
     * -> 删除cookie里的token_key
     *
     * @return
     */
    @GetMapping("/logout")
    public ResponseResult logout() {
        return userService.doLogout();
    }

    /**
     * 获取二维码
     * 二维码的图片路径
     * 二维码的内容字符串
     * TODO: 接口防止爆破
     *
     * @return
     */
    @GetMapping("/pc_login_qr_code")
    public ResponseResult getPLoginQrCode() {
        return userService.getPLoginQrCodeInfo();
    }

    /**
     * 检查二维码的登录状态
     *
     * @return
     */
    @GetMapping("/qr_code_state/{loginId}")
    public ResponseResult checkQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.checkQrCodeLoginState(loginId);
    }

    @PutMapping("/qr_code_state/{loginId}")
    public ResponseResult updateQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.updateQrCodeLoginState(loginId);
    }

    @GetMapping("/check_token")
    public ResponseResult parseToken() {
        return userService.parseToken();
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/reset_password/{userId}")
    public ResponseResult resetPassword(@PathVariable("userId") String userId, @RequestParam("password") String password) {
        return userService.resetPassword(userId, password);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/register_count")
    public ResponseResult getRegisterCount() {
        return userService.getRegisterCount();
    }

}

