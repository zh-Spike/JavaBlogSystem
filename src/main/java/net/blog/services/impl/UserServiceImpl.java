package net.blog.services.impl;

import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.RefreshTokenDao;
import net.blog.dao.SettingsDao;
import net.blog.dao.UserDao;
import net.blog.dao.UserNoPasswordDao;
import net.blog.pojo.RefreshToken;
import net.blog.pojo.Settings;
import net.blog.pojo.User;
import net.blog.pojo.UserNoPassword;
import net.blog.response.ResponseResult;
import net.blog.response.ResponseState;
import net.blog.services.IUserService;
import net.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class UserServiceImpl extends BaseService implements IUserService {

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

    @Autowired
    private TaskService taskService;

    @Autowired
    private Gson gson;

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
        settings.setCreateTime(new Date());
        settings.setUpdateTime(new Date());
        settings.setValue("1");
        settingsDao.save(settings);

        return ResponseResult.SUCCESS("初始化成功");
    }

    @Autowired
    private Random random;

    @Autowired
    private RedisUtils redisUtils;

    public static final int[] captcha_font_types = {Captcha.FONT_1
            , Captcha.FONT_2
            , Captcha.FONT_3
            , Captcha.FONT_4
            , Captcha.FONT_5
            , Captcha.FONT_6
            , Captcha.FONT_7
            , Captcha.FONT_8
            , Captcha.FONT_9
            , Captcha.FONT_10};


    @Override
    public void createCaptcha(HttpServletResponse response) throws Exception {
//        if (TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13) {
//            return;
//        }

        // 放弃通过时间来获得验证码
        // 优化性能
        // 防止重复创建 占用redis太多资源
        // 检查上一次的id 如果有就重复利用
        String lastId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_CAPTCHA_ID);
        String key;
        if (TextUtils.isEmpty(lastId)) {
            key = idWorker.nextId() + "";
        } else {
            key = lastId;
        }
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int captchaType = random.nextInt(3);
        Captcha targetCaptcha = null;
        if (captchaType == 0) {
            // 三个参数分别为宽、高、位数
            targetCaptcha = new SpecCaptcha(Constants.CAPTCHA_WIDTH, Constants.CAPTCHA_HEIGHT, 5);
        } else if (captchaType == 1) {
            // gif类
            targetCaptcha = new GifCaptcha(Constants.CAPTCHA_WIDTH, Constants.CAPTCHA_HEIGHT);
        } else {
            // 算术类
            targetCaptcha = new ArithmeticCaptcha(Constants.CAPTCHA_WIDTH, Constants.CAPTCHA_HEIGHT);
            targetCaptcha.setLen(2); // 几位数运算
        }
        // 设置字体
        // specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        int index = random.nextInt(captcha_font_types.length);
        log.info("captcha font type index ==> " + index);
        targetCaptcha.setFont(captcha_font_types[index]);
        // 设置类型，纯数字、纯字母、字母数字混合
        // specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        targetCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String content = targetCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);
        // 存入redis
        // 删除时机
        // 1. 自然过期
        // 2. 验证码用完就删
        // 3.用完的情况：get
        // 把之前id写道cookie里面,用于后面检查
        CookieUtils.setUpCookie(response, Constants.User.LAST_CAPTCHA_ID, key);
        redisUtils.set(Constants.User.KEY_CAPTCHA_CONTENT + key, content, 60 * 10);
        targetCaptcha.out(response.getOutputStream());
    }

    /**
     * 发生邮件验证码
     * 注册、找回密码、修改邮箱
     * 注册：判断是否注册过了
     * 找回：如果没注册，提示未注册
     * 修改：如果新邮箱已经注册，提示修改密码
     *
     * @param type
     * @param request
     * @param emailAddress
     * @return ResponseResult
     */
    @Override
    public ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress, String captchaCode) {
        // 检查图灵验证码是否正确
        // 从cookies里拿key
        String captchaId = CookieUtils.getCookie(request, Constants.User.LAST_CAPTCHA_ID);
        String captchaValue = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaId);
        if (!captchaCode.equals(captchaValue)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        }
        if (emailAddress == null) {
            return ResponseResult.FAILED("邮箱地址不能为空");
        }
        // 根据类型查询邮箱是否存在
        if ("register".equals(type) || "update".equals(type)) {
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
        log.info("Constants.User.KEY_EMAIL_SEND_IP + remoteAddr == > " + Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        String ipSendTimeValue = (String) redisUtils.get(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        Integer ipSendTime;
        if (ipSendTimeValue != null) {
            ipSendTime = Integer.parseInt(ipSendTimeValue);
        } else {
            ipSendTime = 1;
        }
        if (ipSendTime > 10) {
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
        //update
        if (ipSendTime == null) {
            ipSendTime = 0;
        }
        ipSendTime++;
        // 1小时有效期
        redisUtils.set(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr, String.valueOf(ipSendTime), 60 * 60);
        redisUtils.set(Constants.User.KEY_EMAIL_SEND_ADDRESS + emailAddress, "true", 30);
        // 保存code, 10分钟有效
        redisUtils.set(Constants.User.KEY_EMAIL_CODE_CONTENT + emailAddress, String.valueOf(code), 60 * 10);
        return ResponseResult.SUCCESS("验证码发送成功");
    }

    @Override
    public ResponseResult register(User user, String emailCode, String captchaCode) {
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
        // captchaKey从redis里拿
        String captchaKey = CookieUtils.getCookie(getRequest(), Constants.User.LAST_CAPTCHA_ID);
        if (TextUtils.isEmpty(captchaKey)) {
            return ResponseResult.FAILED("请允许保留Cookie信息");
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
        String ipAddress = getRequest().getRemoteAddr();
        user.setRegIp(ipAddress);
        user.setLoginIp(ipAddress);
        user.setUpdateTime(new Date());
        user.setCreateTime(new Date());
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setState("1");
        user.setSign("");
        user.setId(idWorker.nextId() + "");
        //第八步：保存到数据库
        CookieUtils.deleteCookie(getResponse(), Constants.User.LAST_CAPTCHA_ID);
        userDao.save(user);
        //第九步：返回结果
        return ResponseResult.GET(ResponseState.JOIN_IN_SUCCESS);
    }


    @Override
    public ResponseResult doLogin(String captcha,
                                  User user,
                                  String from) {
        // from可能没有值 给一个默认
        if (TextUtils.isEmpty(from)
                || (!Constants.FROM_MOBILE.equals(from) && !Constants.FORM_PC.equals(from))) {
            from = Constants.FROM_MOBILE;
        }
        String captchaKey = CookieUtils.getCookie(getRequest(), Constants.User.LAST_CAPTCHA_ID);
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        String captchaValue = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (!captcha.equals(captchaValue)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        }
        // 验证成功，删除redis里的数据
        redisUtils.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
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
            userFromDb = userDao.findOneByEmail(userName);
        }

        if (userFromDb == null) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        // 用户存在
        // 对比密码
        boolean matches = bCryptPasswordEncoder.matches(password, userFromDb.getPassword());
        if (!matches) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        // 判断用户状态
        if (!"1".equals(userFromDb.getState())) {
            return ResponseResult.ACCOUNT_DENIED();
        }
        // 修改更新时间和登录IP
        userFromDb.setLoginIp(request.getRemoteAddr());
        userFromDb.setUpdateTime(new Date());
        createToken(response, userFromDb, from);
        CookieUtils.deleteCookie(getResponse(), Constants.User.LAST_CAPTCHA_ID);
        return ResponseResult.SUCCESS("登录成功");
    }

    /**
     * @param response
     * @param userFromDb
     * @param from
     * @return tokenKey
     */
    private String createToken(HttpServletResponse response, User userFromDb, String from) {
        String oleTokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        // 不能直接删除了
        RefreshToken oldRefreshToken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        // 根据来源删除refreshToken中对应的tokenKey
        if (Constants.FROM_MOBILE.equals(from)) {
            // 单端登录删掉redis里的token
            if (oldRefreshToken != null) {
                redisUtils.del(Constants.User.KEY_TOKEN + oldRefreshToken.getMobileTokenKey());
            }
            refreshTokenDao.deleteMobileTokenKey(oleTokenKey);
        } else if (Constants.FORM_PC.equals(from)) {
            // 确保单端登录删除redis里的token
            if (oldRefreshToken != null) {
                redisUtils.del(Constants.User.KEY_TOKEN + oldRefreshToken.getTokenKey());
            }
            refreshTokenDao.deletePCTokenKey(oleTokenKey);
        }
        // 密码正确,生成Token claims已经有from了
        Map<String, Object> claims = ClaimsUtils.user2Claims(userFromDb, from);
        // token有效2小时
        String token = JwtUtils.createToken(claims);
        // 返回token MD5,token保存在redis里
        // 前端访问取token的MD5key，从redis读取
        String tokenKey = from + DigestUtils.md5DigestAsHex(token.getBytes());
        // 保存token到redis,有效期2h，key为tokenKey
        redisUtils.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValueInSecond.HOUR_2);
        // 把tokenKey写到cookies
        CookieUtils.setUpCookie(response, Constants.User.COOKIE_TOKEN_KEY, tokenKey);
        // 先判断数据库里有无refreshToken
        // 有就更新 没有就创建
        RefreshToken refreshToken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setId(idWorker.nextId() + "");
            refreshToken.setCreateTime(new Date());
            refreshToken.setUserId(userFromDb.getId());
        }
        // 不过是过期还是新登录 都会生成 refreshToken
        // 生成refreshToken 单位毫秒 * 1000
        String refreshTokenValue = JwtUtils.createRefreshToken(userFromDb.getId(), Constants.TimeValueInMillions.MONTH);
        // 保存到数据库
        refreshToken.setRefreshToken(refreshTokenValue);
        // 判断来源 移动端和PC
        if (Constants.FORM_PC.equals(from)) {
            refreshToken.setTokenKey(tokenKey);
        } else {
            refreshToken.setMobileTokenKey(tokenKey);
        }
        refreshToken.setUpdateTime(new Date());
        refreshTokenDao.save(refreshToken);
        return tokenKey;
    }

    /**
     * 本质，通过token_key检查用户是否登录，如果有，则返回用户信息
     *
     * @param
     * @param
     * @return
     */
    @Override
    public User checkUser() {
        // 拿tokenKey
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        log.info("checkUser tokenKey == > " + tokenKey);
        if (TextUtils.isEmpty(tokenKey)) {
            return null;
        }
        User user = parseByTokenKey(tokenKey);
        // 从Token中解析出此请求时什么端的
        String from = tokenKey.startsWith(Constants.FORM_PC) ? Constants.FORM_PC : Constants.FROM_MOBILE;
        if (user == null) {
            // 有token,解析
            // 报错，token过期
            // 1.去MySQL查refreshToken
            // 从pc来的 从pc的token_key来查
            // 如果是mobile的 从mobile_key来查
            RefreshToken refreshToken;
            if (Constants.FORM_PC.equals(from)) {
                refreshToken = refreshTokenDao.findOneByTokenKey(tokenKey);
            } else {
                refreshToken = refreshTokenDao.findOneByMobileTokenKey(tokenKey);
            }
            // 2.不存在,重新登录
            if (refreshToken == null) {
                log.info("refresh token is null...");
                return null;
            }
            // 3.存在,解析refreshToken
            try {
                // 解析可能会出错 就可能是过期了
                JwtUtils.parseJWT(refreshToken.getRefreshToken());
                // 4.如果refreshToken有效，创建新的token和refreshToken
                String userId = refreshToken.getUserId();
                User userFromDb = userDao.findOneById(userId);
                // 不能直接setPassword,会重置数据库密码
                // 删除refreshToken记录
                String newTokenKey = createToken(getResponse(), userFromDb, from);
                // 返回token
                log.info("create new token and refresh token...");
                return parseByTokenKey(newTokenKey);
            } catch (Exception e1) {
                log.info("refresh token is expired...");
                // 5.如果refreshToken过期，则返回没有登陆
                return null;
            }
        }
        return user;
    }

    @Override
    public ResponseResult getUserInfo(String userId) {
        // 从数据库中获取
        User user = userDao.findOneById(userId);
        // 判断结果
        if (user == null) {
            // 如果不存在，就返回不存在
            return ResponseResult.FAILED("用户不存在");
        }
        // 如果存在就返回对象，清空密码等数据
        String userJson = gson.toJson(user);
        User newUser = gson.fromJson(userJson, User.class);
        newUser.setPassword("");
        newUser.setEmail("");
        newUser.setRegIp("");
        newUser.setLoginIp("");
        return ResponseResult.SUCCESS("获取成功").setData(newUser);
    }

    @Override
    public ResponseResult checkEmail(String email) {
        User user = userDao.findOneByEmail(email);
        return user == null ? ResponseResult.FAILED("该邮箱未注册") : ResponseResult.SUCCESS("该邮箱已注册");
    }

    @Override
    public ResponseResult checkUserName(String userName) {
        User user = userDao.findOneByUserName(userName);
        return user == null ? ResponseResult.FAILED("该用户名未注册") : ResponseResult.SUCCESS("该用户名已存在");
    }

    /**
     * 更新用户信息
     *
     * @param userId
     * @param user
     * @return
     */
    @Override
    public ResponseResult updateUserInfo(String userId, User user) {
        User userFromTokenKey = checkUser();
        // 从token中解析
        if (userFromTokenKey == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        User userFromDb = userDao.findOneById(userFromTokenKey.getId());

        // 判断ID是否一致，一致才能修改
        if (!userFromDb.getId().equals(userId)) {
            return ResponseResult.PERMISSION_DENIED();
        }
        // 用户名
        String userName = user.getUserName();
        if (!TextUtils.isEmpty(userName) && !userName.equals(userFromTokenKey.getUserName())) {
            User userByUserName = userDao.findOneByUserName(userName);
            if (userByUserName != null) {
                return ResponseResult.FAILED("该用户名已注册");
            }
            userFromDb.setUserName(userName);
        }
        // 头像
        if (!TextUtils.isEmpty(user.getAvatar())) {
            userFromDb.setAvatar(user.getAvatar());
        }
        userFromDb.setUpdateTime(new Date());
        // 签名 可为空
        userFromDb.setSign(user.getSign());
        userDao.save(userFromDb);
        // 更新redis里的token
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        redisUtils.del(Constants.User.KEY_TOKEN + tokenKey);
        return ResponseResult.SUCCESS("用户更新成功");
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    private HttpServletResponse getResponse() {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    /**
     * 删除不是真的删除，改用户状态
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseResult deleteUserById(String userId) {
        // 可以操作
        int result = userDao.deleteUserByState(userId);
        if (result > 0) {
            return ResponseResult.SUCCESS("删除成功");
        } else {
            return ResponseResult.FAILED("用户不存在");
        }
    }

    /**
     * 权限：admin
     *
     * @param page
     * @param size
     * @return
     */
    @Autowired
    private UserNoPasswordDao userNoPasswordDao;

    @Override
    public ResponseResult listUsers(int page, int size, String userName, String email) {
        // 可以操作
        // 分页查询
        page = checkPage(page);
        size = checkSize(size);
        // 根据注册日期来排序
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<UserNoPassword> all = userNoPasswordDao.findAll(new Specification<UserNoPassword>() {
            @Override
            public Predicate toPredicate(Root<UserNoPassword> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!TextUtils.isEmpty(userName)) {
                    Predicate preUser = criteriaBuilder.like(root.get("userName").as(String.class), "%" + userName + "%");
                    predicates.add(preUser);
                }
                if (!TextUtils.isEmpty(email)) {
                    Predicate preEmail = criteriaBuilder.like(root.get("email").as(String.class), email);
                    predicates.add(preEmail);
                }
                Predicate[] preArray = new Predicate[predicates.size()];
                predicates.toArray(preArray);
                return criteriaBuilder.and(preArray);
            }
        }, pageable);
        return ResponseResult.SUCCESS("获取用户列表成功").setData(all);
    }

    /**
     * 更新密码
     *
     * @param verifyCode
     * @param user
     * @return
     */
    @Override
    public ResponseResult updateUserPassword(String verifyCode, User user) {
        //检查邮箱是否有填写
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱不可以为空.");
        }
        //根据邮箱去redis里拿验证
        //进行对比
        String redisVerifyCode = (String) redisUtils.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误.");
        }
        redisUtils.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        int result = userDao.updatePasswordByEmail(bCryptPasswordEncoder.encode(user.getPassword()), email);
        //修改密码
        return result > 0 ? ResponseResult.SUCCESS("密码修改成功") : ResponseResult.FAILED("密码修改失败");
    }

    /**
     * 更新邮箱
     *
     * @param email
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updateEmail(String email, String verifyCode) {
        //1、确保用户已经登录了
        User user = this.checkUser();
        //没有登录
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、对比验证码，确保新的邮箱地址是属于当前用户的
        String redisVerifyCode = (String) redisUtils.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(redisVerifyCode) || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误");
        }
        // 正确，删除redis里的验证码
        redisUtils.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);

        //可以修改邮箱
        int result = userDao.updateEmailById(email, user.getId());
        return result > 0 ? ResponseResult.SUCCESS("邮箱修改成功") : ResponseResult.FAILED("邮箱修改失败");
    }

    @Override
    public ResponseResult doLogout() {
        // 拿到一个token_key
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 删除redis里的token 各端口是独立的
        redisUtils.del(Constants.User.KEY_TOKEN + tokenKey);
        // 删除mysql里的refreshToken
        // 这个不能删除 更新
        // refreshTokenDao.deleteAllByTokenKey(tokenKey);
        if (Constants.FORM_PC.startsWith(tokenKey)) {
            refreshTokenDao.deletePCTokenKey(tokenKey);
        } else {
            refreshTokenDao.deleteMobileTokenKey(tokenKey);
        }
        // 删除cookie
        CookieUtils.deleteCookie(getResponse(), Constants.User.COOKIE_TOKEN_KEY);
        return ResponseResult.SUCCESS("退出登陆成功.");
    }

    @Override
    public ResponseResult getPLoginQrCodeInfo() {
        // 尝试取出上次的loginId
        String lastLoginId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_REQUEST_LOGIN_ID);
        if (!TextUtils.isEmpty(lastLoginId)) {
//            // 先删除redis
//            redisUtils.del(Constants.User.KEY_PC_LOGIN_ID + lastLoginId);
//            // 检查上次请求时间 太频繁直接拦截
//            Object lastGetTime = redisUtils.get(Constants.User.LAST_REQUEST_LOGIN_ID + lastLoginId);
//            if (lastGetTime != null) {
//                return ResponseResult.FAILED("服务器繁忙");
//            }
        }
        // 1. 生成一个唯一的ID
        long code;
        if (!TextUtils.isEmpty(lastLoginId)) {
            code = Long.parseLong(lastLoginId);
        } else {
            code = idWorker.nextId();
        }
        // 2. 保存到redis 值为false 时间为5minutes 二维码的有效期
        redisUtils.set(Constants.User.KEY_PC_LOGIN_ID + code, Constants.User.KEY_PC_LOGIN_STATE_FALSE,
                Constants.TimeValueInSecond.MIN_5);
        Map<String, Object> result = new HashMap<>();
        String originalDomain = TextUtils.getDomain(getRequest());
        result.put("code", String.valueOf(code));
        result.put("url", originalDomain + "/portal/image/qr_code/" + code);
        CookieUtils.setUpCookie(getResponse(), Constants.User.LAST_REQUEST_LOGIN_ID, String.valueOf(code));
//        redisUtils.set(Constants.User.LAST_REQUEST_LOGIN_ID + String.valueOf(code),
//                "true", Constants.TimeValueInSecond.SECOND_10);
        // 返回结果
        return ResponseResult.SUCCESS("获取成功").setData(result);
    }

    @Autowired
    private CountDownLatchManager countDownLatchManager;

    /**
     * 检查二维码的登录状态
     * 结果有：
     * 1、登录成功 loginId对应的值为有ID内容
     * 2、等待扫描 loginId对应的值为false
     * 3、二维码已经过期了 loginId对应的值为null
     * <p>
     * 是被PC端轮询调用的
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult checkQrCodeLoginState(String loginId) {
        //从redis里取值出来
        ResponseResult result = checkLoginIdState(loginId);
        if (result != null) return result;
        // 先等待一会 再去检查
        // 超出这个时间我们就返回等待扫码
        Callable<ResponseResult> callable = new Callable<ResponseResult>() {
            @Override
            public ResponseResult call() throws Exception {
                log.info("start waiting for scan...");
                // 先阻塞
                countDownLatchManager.getLatch(loginId).await(Constants.User.QR_CODE_STATE_CHECK_WAITING_TIME,
                        TimeUnit.SECONDS);
                // 收到状态更新的通知后
                log.info("start check login state...");
                ResponseResult checkResult = checkLoginIdState(loginId);
                if (checkResult != null)
                    return checkResult;
                // 超时后返回等待扫描
                // 完事后删除对应latch
                countDownLatchManager.deleteLatch(loginId);
                CookieUtils.deleteCookie(getResponse(), Constants.User.LAST_CAPTCHA_ID);
                return ResponseResult.WAITING_FOR_SCAN();
            }
        };
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.WAITING_FOR_SCAN();
    }

    /**
     * 更新二维码的登录状态
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult updateQrCodeLoginState(String loginId) {
        //1、检查用户是否登录
        User User = checkUser();
        if (User == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、改变loginId对应的值=true
        redisUtils.set(Constants.User.KEY_PC_LOGIN_ID + loginId, User.getId());
        //2.1、通知正在等待的扫描任务
        countDownLatchManager.onPhoneDoLogin(loginId);
        //3、返回结果
        return ResponseResult.SUCCESS("登录成功.");
    }

    @Override
    public ResponseResult parseToken() {
        User user = checkUser();
        if (user == null) {
            return ResponseResult.FAILED("用户未登录");
        }
        return ResponseResult.SUCCESS("获取登录信息成功").setData(user);
    }

    @Override
    public ResponseResult resetPassword(String userId, String password) {
        // 查询用户
        User user = userDao.findOneById(userId);
        // 判断用户是否存在
        if (user == null) {
            return ResponseResult.FAILED("用户不存在");
        }
        // 对密码进行加密
        user.setPassword(bCryptPasswordEncoder.encode(password));
        // 处理结果
        userDao.save(user);
        return ResponseResult.SUCCESS("密码重置成功");
    }

    @Override
    public ResponseResult getRegisterCount() {
        long count = userDao.count();
        return ResponseResult.SUCCESS("获取用户总数成功").setData(count);
    }

    @Override
    public ResponseResult checkEmailCode(String email, String emailCode, String captchaCode) {
        // 检查人类验证码是否正确
        String captchaId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_CAPTCHA_ID);
        String captcha = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaId);
        if (!captchaCode.equals(captcha)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        }
        // 检查邮箱验证码
        String redisVerifyCode = (String) redisUtils.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (!emailCode.equals(redisVerifyCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        }
        return ResponseResult.SUCCESS("邮箱验证码正确");
    }

    private ResponseResult checkLoginIdState(String loginId) {
        String loginState = (String) redisUtils.get(Constants.User.KEY_PC_LOGIN_ID + loginId);
        if (loginState == null) {
            // 二维码过期
            return ResponseResult.QR_CODE_DEPRECATE();
        }
        // 不为false 且状态不为空 那就是用户ID 登陆成功
        if (!TextUtils.isEmpty(loginState) &&
                !Constants.User.KEY_PC_LOGIN_STATE_FALSE.equals(loginState)) {
            // 创建token 走PC端登录
            User userFromDb = userDao.findOneById(loginState);
            if (userFromDb == null) {
                return ResponseResult.QR_CODE_DEPRECATE();
            }
            createToken(getResponse(), userFromDb, Constants.FORM_PC);
            CookieUtils.deleteCookie(getResponse(), Constants.User.LAST_REQUEST_LOGIN_ID);
            // 登录成功
            return ResponseResult.LOG_IN_SUCCESS();
        }
        return null;
    }

    /**
     * 解析此token是从PC还是移动端来的 判空
     *
     * @param tokenKey
     * @return
     */
    private String parseFrom(String tokenKey) {
        String token = (String) redisUtils.get(Constants.User.KEY_TOKEN + tokenKey);
        if (token != null) {
            try {
                Claims claims = JwtUtils.parseJWT(token);
                return ClaimsUtils.getFrom(claims);
            } catch (Exception e) {
                log.info("parseByTokenKey == >" + tokenKey + "expired");
                return null;
            }
        }
        return null;
    }

    private User parseByTokenKey(String tokenKey) {
        String token = (String) redisUtils.get(Constants.User.KEY_TOKEN + tokenKey);
        log.info("parseByTokenKey token == >" + token);
        if (token != null) {
            try {
                Claims claims = JwtUtils.parseJWT(token);
                return ClaimsUtils.claims2User(claims);
            } catch (Exception e) {
                log.info("parseByTokenKey == >" + tokenKey + "expired");
                return null;
            }
        }
        return null;
    }
}