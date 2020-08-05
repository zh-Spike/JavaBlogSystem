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
     *初始化管理员账号 init-admin
     */
    @PostMapping("/admin_account")
    public ResponseResult initManagerAccount(@RequestBody User user, HttpServletRequest request){
        log.info("user name ==> " + user.getUser_name());
        log.info("password ==> " + user.getPassword());
        log.info("email ==> " + user.getEmail());
        return  userService.initManagerAccount(user, request);
    }
    /**
     *注册
     * @param user
     * @return
     */
    @PostMapping
    public ResponseResult register(@RequestBody User user){
        //第一步：检查当前用户名是否已经注册
        //第二步：检查邮箱格式是否正确
        //第三步：检查该邮箱是否已经注册
        //第四步：检查邮箱验证码是否正确
        //第五步：检查CAPTCHA是否正确
        //第六步：对密码进行加密
        //第七步：补全数据
        //注册IP，登录IP，角色，头像，创建时间，登陆时间
        //第八步：保存到数据库
        //第九步：返回结果
        return null;
    }
    /**
     * 登录sign-up
     *
     * @param captcha
     * @param user
     * @return
     */
    @PostMapping("/{captcha}")
    public ResponseResult login(@PathVariable("captcha") String captcha,@RequestBody User user){
        return null;
    }

    /**
     * 验证码
     * 有效时长10mins
     * @return
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response,@RequestParam("captcha_key")String captchaKey) {
        try {
            userService.createCaptcha(response, captchaKey);
        } catch (Exception e){
            log.error(response.toString());
        }
    }
    /**
     *发邮件
     * @return
     */
    @GetMapping("/verify_code")
    public ResponseResult sendVerifyCode(HttpServletRequest request,@RequestParam("email") String emailAddress){
        log.info("email ==>"+ emailAddress);
        return userService.sendEmail(request, emailAddress);
    }

    /**
     * 修改密码 UpdatePassword
     *
     * @return
     */

    @PutMapping("/password")
    public ResponseResult updatePassword(@RequestBody User user){
        return null;
    }

    /**
     * 作者信息user-info
     *
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId){
        return null;
    }
    /**
     * 修改信息user-info
     *
     * @return
     */
    @PutMapping
    public ResponseResult updateUserInfo(@RequestBody User user){
        return null;
    }

    @GetMapping("/list")
    public ResponseResult listUser(@RequestParam("page") int page,@RequestParam("size")int size){
        return null;
    }

    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId")String userId){
        return null;
    }

}



