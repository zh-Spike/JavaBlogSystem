package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.SettingsDao;
import net.blog.dao.UserDao;
import net.blog.pojo.Settings;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements IUserService {

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
        if (managerAccountState == null){
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
}
