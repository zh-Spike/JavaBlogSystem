package net.blog.services.impl;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.RefreshTokenDao;
import net.blog.dao.SettingsDao;
import net.blog.dao.UserDao;
import net.blog.pojo.RefreshToken;
import net.blog.pojo.Settings;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.response.ResponseState;
import net.blog.services.IUserService;
import net.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SettingsDao settingsDao;

    @Autowired
    private RefreshTokenDao refreshTokenDao;


    private Cookie[] cookies;
    private String tokenKey;
    private Object token;

    @Override
    public ResponseResult initManagerAccount(User user, HttpServletRequest request) {
        //检查是否有初始化
        Settings managerAccountState = settingsDao.findOneByKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        if (managerAccountState != null) {
            return ResponseResult.FAILED("管理员账号已经初始化了");
        }
        //检查数据
        if (TextUtils.isEmpty(user.getUserName())) {
            return ResponseResult.FAILED("用户名不能为空");
        }
        if (TextUtils.isEmpty(user.getPassword())) {
            return ResponseResult.FAILED("密码不能为空");
        }
        if (TextUtils.isEmpty(user.getEmail())) {
            return ResponseResult.FAILED("邮箱不能为空");
        }

        //补充数据
        user.setId(String.valueOf(idWorker.nextId()));
        user.setRoles(Constants.User.ROLE_ADMIN);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr = request.getRemoteAddr();
        String localAddr = request.getLocalAddr();
        log.info("remoteAddr ==>" + remoteAddr);
        log.info("localAddr ==>" + localAddr);
        user.setLoginIp(remoteAddr);
        user.setRegIp(remoteAddr);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //对密码进行加密
        //原密码
        String password = user.getPassword();
        //加密码
        String encode = bCryptPasswordEncoder.encode(password);
        user.setPassword(encode);
        //存到数据库
        userDao.save(user);
        //改标记
        Settings settings = new Settings();
        settings.setId(idWorker.nextId() + "");
        settings.setKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        settings.setCreate_time(new Date());
        settings.setUpdate_time(new Date());
        settings.setValue("1");
        settingsDao.save(settings);

        return ResponseResult.SUCCESS("初始化成功");
    }

    public static final int[] catcha_font_types = {Captcha.FONT_1
            , Captcha.FONT_2
            , Captcha.FONT_3
            , Captcha.FONT_4
            , Captcha.FONT_5
            , Captcha.FONT_6
            , Captcha.FONT_7
            , Captcha.FONT_8
            , Captcha.FONT_9
            , Captcha.FONT_10};

    @Autowired
    private Random random;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception {
        if (TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13) {
            return;
        }
        long key;
        try {
            key = Long.parseLong(captchaKey);
        } catch (Exception e) {
            return;
        }
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int captchaType = random.nextInt(3);
        Captcha targetCaptcha = null;
        if (captchaType == 0) {
            // 三个参数分别为宽、高、位数
            targetCaptcha = new SpecCaptcha(200, 60, 5);
        } else if (captchaType == 1) {
            // gif类
            targetCaptcha = new GifCaptcha(200, 60);
        } else if (captchaType == 2) {
            // 算术类
            targetCaptcha = new ArithmeticCaptcha(200, 60);
            targetCaptcha.setLen(2); // 几位数运算
        }
        // 设置字体
        // specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        int index = random.nextInt(catcha_font_types.length);
        log.info("captcha font type index ==> " + index);
        targetCaptcha.setFont(catcha_font_types[index]);
        // 设置类型，纯数字、纯字母、字母数字混合
        // specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        targetCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String content = targetCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);
        // 存入redis
        redisUtils.set(Constants.User.KEY_CAPTCHA_CONTENT + key, content, 60 * 10);
        targetCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private TaskService taskService;

    /**
     * 发生邮件验证码
     * 注册、找回密码、修改邮箱
     * 注册：判断是否注册过了
     * 找回：如果没注册，提示未注册
     * 修改：如果新邮箱已经注册，提示修改密码
     *
     * @param request
     * @param emailAddress
     * @return
     */
    @Override
    public ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress) {
        if (emailAddress == null) {
            return ResponseResult.FAILED("邮箱地址不能为空");
        }
        // 根据类型查询邮箱是否存在
        if ("regiser".equals(type) || "updtae".equals(type)) {
            User userByEmail = userDao.findOneByEmail(emailAddress);
            if (userByEmail != null) {
                return ResponseResult.FAILED("该邮箱已注册");
            }
        } else if ("forget".equals(type)) {
            User userByEmail = userDao.findOneByEmail(emailAddress);
            if (userByEmail == null) {
                return ResponseResult.FAILED("该邮箱未注册");
            }
        }
        // 1.防暴力发送:同个邮箱间隔要超过30s，同个ip最多发10次，1h内短信最多3次
        String remoteAddr = request.getRemoteAddr();
        log.info("sendEmail ==>  ip ==> " + remoteAddr);
        if (remoteAddr != null) {
            remoteAddr = remoteAddr.replaceAll(":", "_");
        }
        // 取，如果没有 通过
        Integer ipSendTime = (Integer) redisUtils.get(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        if (ipSendTime != null && ipSendTime > 10) {
            return ResponseResult.FAILED("验证码发的也太多了吧");
        }
        Object hasEmailSend = redisUtils.get(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress);
        if (hasEmailSend != null) {
            return ResponseResult.FAILED("验证码发的也太多了吧");
        }
        // 2.检查地址是否正确
        boolean isEmailFormatRight = TextUtils.isEmailAddressRight(emailAddress);
        if (!isEmailFormatRight) {
            return ResponseResult.FAILED("邮箱地址格式不正确");
        }
        // 3.发送验证码,6位
        int code = random.nextInt(999999);
        if (code < 100000) {
            code += 100000;
        }
        log.info("sendEmail ==> " + code);
        try {
            taskService.sendEmailVerifyCode(String.valueOf(code), emailAddress);
        } catch (Exception e) {
            return ResponseResult.FAILED("验证码发送失败，请稍后重试");
        }
        // 4.记录
        // 发送记录，code
        //
        if (ipSendTime == null) {
            ipSendTime = 0;
        }
        ipSendTime++;
        // 1小时有效期
        redisUtils.set(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr, ipSendTime, 60 * 60);
        redisUtils.set(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress, "true", 30);
        // 保存code, 10分钟有效
        redisUtils.set(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress, String.valueOf(code), 60 * 10);
        return ResponseResult.SUCCESS("验证码发送成功");
    }

    @Override
    public ResponseResult register(User user, String emailCode, String captchaCode, String captchaKey, HttpServletRequest request) {
        //第一步：检查当前用户名是否已经注册
        String userName = user.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("用户名不能为空");
        }
        User userByName = userDao.findOneByUserName(userName);
        if (userByName != null) {
            return ResponseResult.FAILED("该用户名已注册");
        }
        //第二步：检查邮箱格式是否正确
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱地址不能为空");
        }
        if (!TextUtils.isEmailAddressRight(email)) {
            return ResponseResult.FAILED("邮箱地址格式不正确");
        }
        //第三步：检查该邮箱是否已经注册
        User userByEmail = userDao.findOneByEmail(email);
        if (userByEmail != null) {
            return ResponseResult.FAILED("该邮箱地址已注册");
        }
        //第四步：检查邮箱验证码是否正确
        String emailVerifyCode = (String) redisUtils.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(emailVerifyCode)) {
            return ResponseResult.FAILED("邮箱验证码已过期");
        }
        if (!emailVerifyCode.equals(emailCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        } else {
            //正确，删除redis
            redisUtils.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        }
        //第五步：检查CAPTCHA是否正确
        String captchaVerifyCode = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (TextUtils.isEmpty(captchaVerifyCode)) {
            return ResponseResult.FAILED("图灵验证码已过期");
        }
        if (!captchaVerifyCode.equals(captchaCode)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        } else {
            //正确，删除redis
            redisUtils.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        }
        // 通过可注册
        // 第六步：对密码进行加密
        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不能为空");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        //第七步：补全数据
        //注册IP，登录IP，角色，头像，创建时间，登陆时间
        String ipAddress = request.getRemoteAddr();
        user.setRegIp(ipAddress);
        user.setLoginIp(ipAddress);
        user.setUpdateTime(new Date());
        user.setCreateTime(new Date());
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setState("1");
        user.setId(idWorker.nextId() + "");
        //第八步：保存到数据库
        userDao.save(user);
        //第九步：返回结果
        return ResponseResult.GET(ResponseState.JOIN_IN_SUCCESS);
    }

    @Override
    public ResponseResult doLogin (String captcha,
                                  String captchaKey,
                                  User user,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        String captchaValue = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (!captcha.equals(captchaValue)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        }
        // 有可能是邮箱，也可能是用户名
        String userName = user.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("账号不能为空");
        }
        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不能为空");
        }

        User userFromDb = userDao.findOneByUserName(userName);
        if (userFromDb == null) {
            userFromDb = userDao.findOneByUserName(userName);
        }

        if (userFromDb == null) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        // 用户存在
        // 对比密码
        Boolean matches = bCryptPasswordEncoder.matches(password, userFromDb.getPassword());
        if (!matches) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        // 判断用户状态
        if (!"1".equals(userFromDb.getState())) {
            return ResponseResult.FAILED("当前账号已被禁止.");
        }


        // 密码正确,生成Token
        Map<String, Object> claims = ClaimsUtils.user2Claims(userFromDb);
        // token有效2小时
        String token = JwtUtils.createToken(claims);
        // 返回token MD5,token保存在redis里
        // 前端访问取token的MD5key，从redis读取
        String tokenKey = DigestUtils.md5DigestAsHex(token.getBytes());
        // 保存token到redis,有效期2h，key为tokenkey
        redisUtils.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValue.HOUR_2);
        // 把tokenkey写到cookies
        CookieUtils.setUpCookie(response, Constants.User.COOKIE_TOKEN_KEY, tokenKey);
        // 生成refreshToken
        String refreshTokenValue = JwtUtils.createRefreshToken(userFromDb.getId(), Constants.TimeValue.MONTH);
        // 保存到数据库
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(idWorker.nextId() + "");
        refreshToken.setRefreshToken(refreshTokenValue);
        refreshToken.setUserId(userFromDb.getId());
        refreshToken.setTokenKey(tokenKey);
        refreshToken.setCreateTime(new Date());
        refreshToken.setUpdateTime(new Date());
        refreshTokenDao.save(refreshToken);
        return ResponseResult.SUCCESS("登录成功");
    }

}
