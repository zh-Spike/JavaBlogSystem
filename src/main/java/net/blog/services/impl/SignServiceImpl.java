package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.AppointmentDao;
import net.blog.dao.LabDao;
import net.blog.dao.SignDao;
import net.blog.pojo.Appointment;
import net.blog.pojo.Lab;
import net.blog.pojo.Sign;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ISignService;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class SignServiceImpl extends BaseService implements ISignService {
    @Autowired
    private IUserService userService;

    @Autowired
    private LabDao labDao;

    @Autowired
    private SignDao signDao;

    @Autowired
    private AppointmentDao appointmentDao;

    @Override
    public ResponseResult signIn(Sign sign) {
        // 感觉性能有点不够 后期可改redis
        User user = userService.checkUser();
        // 先检查数据
        sign.setUserId(user.getId());
        sign.setUserName(user.getUserName());
        sign.setUserAvatar(user.getAvatar());
        if (TextUtils.isEmpty(sign.getLabId())) {
            return ResponseResult.FAILED("实验室不可以为空");
        }
        if (TextUtils.isEmpty(sign.getAppointmentId())) {
            return ResponseResult.FAILED("未预约");
        }
        // 判断实验室容量有点问题
        Appointment appointmentFromDb = appointmentDao.findOneById(sign.getAppointmentId());
        String SignNumberStr = String.valueOf(appointmentFromDb.getAppointmentNumber());
        if (SignNumberStr.equals("0")) {
            return ResponseResult.FAILED("预约人数不能为空");
        }
        String stateStr = appointmentFromDb.getState();
//        log.info("state ==>" + stateStr);
        String isUsedStr = appointmentFromDb.getIsUsed();
//        log.info("user ==>" + isUsedStr);
        Lab labFromDb = labDao.findOneById(sign.getLabId());
        if (stateStr.equals(Constants.Appointment.PASSED)) {
//            log.info("appNum ==>" + appointmentFromDb.getAppointmentNumber());
//            log.info("labNum ==>" + labFromDb.getLabNumber());
            labFromDb.setLabAvailable(labFromDb.getLabNumber() - appointmentFromDb.getAppointmentNumber());
            sign.setNumber(appointmentFromDb.getAppointmentNumber());
        } else {
            return ResponseResult.FAILED("审核未通过");
        }
        if (!isUsedStr.equals(Constants.Appointment.IS_USED)) {
            return ResponseResult.FAILED("该申请已使用");
        }
        appointmentFromDb.setIsUsed(Constants.Appointment.IS_USED);
        // 补全数据
        sign.setLabName(labFromDb.getLabName());
        sign.setId(appointmentFromDb.getId());
        sign.setState(Constants.Sign.SIGN_IN);
        sign.setCreateTime(new Date());
        sign.setUpdateTime(new Date());
        // 保存数据
        signDao.save(sign);
        // 返回结果
        return ResponseResult.SUCCESS("签到成功");
    }

    @Override
    public ResponseResult signOut(String signId) {
        Sign signFromDb = signDao.findOneById(signId);
        if (signFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        userService.checkUser();
        // 获得之前的人数
        Lab labFromDb = labDao.findOneById(signFromDb.getLabId());
        if (signFromDb.getState().equals(Constants.Sign.SIGN_IN)) {
//            log.info("labNum ==>" + labFromDb.getLabNumber());
//            log.info("signNum ==>" + signFromDb.getNumber());
//            log.info("labAvailNum ==>" + labFromDb.getLabAvailable());
            labFromDb.setLabAvailable(signFromDb.getNumber() + labFromDb.getLabAvailable());
            signFromDb.setState(Constants.Sign.SIGN_OUT);
        } else {
            return ResponseResult.FAILED("状态非法");
        }
        // 多次点击会超过上限? 前端禁用按钮 (已解决)
        signFromDb.setUpdateTime(new Date());
        signDao.save(signFromDb);
        return ResponseResult.SUCCESS("签退成功");
    }

    @Override
    public ResponseResult updateSign(String signId) {
        Sign signFromDb = signDao.findOneById(signId);
        if (signFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        // 这样的话不会修改lab人数?
        // admin的签到签退一般没用啊
        // 所以签退还是用signOut那个方法吧
        if (signFromDb.getState().equals(Constants.Sign.NOT_ACTIVE)) {
            Lab labFromDb = labDao.findOneById(signFromDb.getLabId());
            Appointment appointmentFromDb = appointmentDao.findOneById(signFromDb.getAppointmentId());
            labFromDb.setLabAvailable(labFromDb.getLabAvailable() - appointmentFromDb.getAppointmentNumber());
            signFromDb.setState(Constants.Sign.SIGN_IN);
        } else if (signFromDb.getState().equals(Constants.Sign.SIGN_IN)) {
            Lab labFromDb = labDao.findOneById(signFromDb.getLabId());
            labFromDb.setLabAvailable(signFromDb.getNumber() + labFromDb.getLabAvailable());
            signFromDb.setState(Constants.Sign.SIGN_OUT);
        }
        signFromDb.setUpdateTime(new Date());
        signDao.save(signFromDb);
        return ResponseResult.SUCCESS("签到状态修改成功");
    }

    @Override
    public ResponseResult listSigns() {
        // 参数检查
        // 创建条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        // 判断用户 普通/未登录用户  admin权限拉满
        User user = userService.checkUser();
        List<Sign> signs;
        if (Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            signs = signDao.findAll(sort);
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }
        //返回结果
        return ResponseResult.SUCCESS("获取签到列表成功").setData(signs);
    }
}
