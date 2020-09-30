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
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class SignServiceImpl extends BaseService implements ISignService {
    @Autowired
    private IUserService userService;

    @Autowired
    private SnowflakeIdWorker idWorker;

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
        String stateStr = String.valueOf(appointmentFromDb.getState());
        Lab labFromDb = labDao.findOneById(sign.getLabId());
        if (stateStr.equals("2")) {
//            log.info("appNum ==>" + appointmentFromDb.getAppointmentNumber());
//            log.info("labNum ==>" + labFromDb.getLabNumber());
            labFromDb.setLabAvailable(labFromDb.getLabNumber() - appointmentFromDb.getAppointmentNumber());
            sign.setNumber(appointmentFromDb.getAppointmentNumber());
        } else {
            ResponseResult.FAILED("审核未通过");
        }
        // 补全数据
        sign.setId(idWorker.nextId() + "");
        sign.setState("1");
        sign.setCreateTime(new Date());
        sign.setUpdateTime(new Date());
        // 保存数据
        signDao.save(sign);
        // 返回结果
        return ResponseResult.SUCCESS("签到成功");
    }

    @Override
    public ResponseResult signOut(String signId, Sign sign) {
        Sign signFromDb = signDao.findOneById(signId);
        if (signFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        userService.checkUser();
        // 获得之前的人数
        String stateStr = String.valueOf(sign.getState());
        Lab labFromDb = labDao.findOneById(signFromDb.getLabId());
        if (stateStr.equals("2")) {
//            log.info("labNum ==>" + labFromDb.getLabNumber());
//            log.info("signNum ==>" + signFromDb.getNumber());
//            log.info("labAvilNum ==>" + labFromDb.getLabAvailable());
            labFromDb.setLabAvailable(signFromDb.getNumber() + labFromDb.getLabAvailable());
            signFromDb.setState(sign.getState());
        } else {
            return ResponseResult.FAILED("状态非法");
        }
        signFromDb.setUpdateTime(new Date());
        signDao.save(signFromDb);
        return ResponseResult.SUCCESS("签退成功");
    }
}
