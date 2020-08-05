package net.blog.services.impl;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.SettingsDao;
import net.blog.dao.UserDao;
import net.blog.pojo.Settings;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.RedisUtils;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
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

    @Override
    public ResponseResult initManagerAccount(User user, HttpServletRequest request){
        //检查是否有初始化
        Settings managerAccountState = settingsDao.findOneByKey(Constants.Settings.MANAGER_ACCOUNT_INIT_STATE);
        if (managerAccountState != null){
            return ResponseResult.FAILED("管理员账号已经初始化了");
        };
        //检查数据
        if(TextUtils.isEmpty(user.getUser_name())){
            return ResponseResult.FAILED("用户名不能为空");
        }
        if(TextUtils.isEmpty(user.getPassword())){
            return ResponseResult.FAILED("密码不能为空");
        }
        if(TextUtils.isEmpty(user.getEmail())){
            return ResponseResult.FAILED("邮箱不能为空");
        }

        //补充数据
        user.setId(String.valueOf(idWorker.nextId()));
        user.setRoles(Constants.User.ROLE_ADMIN);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr =request.getRemoteAddr();
        String localAddr =request.getLocalAddr();
        log.info("remoteAddr ==>" + remoteAddr);
        log.info("localAddr ==>" + localAddr);
        user.setLogin_ip(remoteAddr);
        user.setReg_ip(remoteAddr);
        user.setCreate_time(new Date());
        user.setUpdate_time(new Date());
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
            ,Captcha.FONT_2
            ,Captcha.FONT_3
            ,Captcha.FONT_4
            ,Captcha.FONT_5
            ,Captcha.FONT_6
            ,Captcha.FONT_7
            ,Captcha.FONT_8
            ,Captcha.FONT_9
            ,Captcha.FONT_10};

    @Autowired
    private Random random;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void createCaptcha(HttpServletResponse response,String captchaKey) throws Exception{
        if (TextUtils.isEmpty(captchaKey) || captchaKey.length() < 13) {
            return;
        }
        long key;
        try {
            key = Long.parseLong(captchaKey);
        }catch (Exception e){
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
        }else if (captchaType == 1){
            // gif类
            targetCaptcha = new GifCaptcha(200,60);
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
        redisUtils.set(Constants.User.KEY_CAPTCHA_CONTENT + key, content, 60 * 10 );
        targetCaptcha.out(response.getOutputStream());
    }
}
