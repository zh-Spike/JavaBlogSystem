package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.AppointmentDao;
import net.blog.pojo.Appointment;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IAppointmentService;
import net.blog.services.IUserService;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class AppointmentImpl extends BaseService implements IAppointmentService {

    @Autowired
    private IUserService userService;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private AppointmentDao appointmentDao;

    @Override
    public ResponseResult addAppointment(Appointment appointment) {
        User user = userService.checkUser();
        // 先检查数据
        appointment.setUserId(user.getId());
        appointment.setUserName(user.getUserName());
        appointment.setUserAvatar(user.getAvatar());
        if (TextUtils.isEmpty(appointment.getLabId())) {
            return ResponseResult.FAILED("实验室不可以为空");
        }
        // 判断实验室容量有点问题
        String appointmentNumberStr = String.valueOf(appointment.getAppointmentNumber());
        if (appointmentNumberStr.equals("0")) {
            return ResponseResult.FAILED("预约人数不能为空");
        }
//        appointment.setStartTime(new Date(String.valueOf(appointment.getStartTime())));
//        appointment.setEndTime(new Date(String.valueOf(appointment.getEndTime())));
        // 补全数据
        appointment.setId(idWorker.nextId() + "");
        appointment.setState("1");
        appointment.setCreateTime(new Date());
        appointment.setUpdateTime(new Date());
        // 保存数据
        appointmentDao.save(appointment);
        // 返回结果
        return ResponseResult.SUCCESS("申请成功");
    }

    @Override
    public ResponseResult deleteAppointment(String appointmentId) {
        int result = appointmentDao.deleteAppointmentByUpdateState(appointmentId);
        if (result == 0) {
            return ResponseResult.FAILED("该预约不存在");
        }
        return ResponseResult.SUCCESS("删除预约成功");
    }

    @Override
    public ResponseResult updateAppointment(String appointmentId, Appointment appointment) {
        // 第一步:找出
        Appointment appointmentFromDb = appointmentDao.findOneById(appointmentId);
        if (appointmentFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        // 第二步：对内容进行判断，有些字段不能为空
        appointmentFromDb.setUpdateTime(new Date());
        String state = appointment.getState();
        if (!TextUtils.isEmpty(state)) {
            appointmentFromDb.setState(state);
        }
        String labId = appointment.getLabId();
        if (!TextUtils.isEmpty(labId)) {
            appointmentFromDb.setLabId(labId);
        }
//        String startTimeStr = String.valueOf(appointment.getStartTime());
//        if (!TextUtils.isEmpty(startTimeStr)) {
//            Date startTime = appointment.getStartTime();
//            appointmentFromDb.setStartTime(startTime);
//        }
//        String endTimeStr = String.valueOf(appointment.getEndTime());
//        if (!TextUtils.isEmpty(endTimeStr)) {
//            Date endTime = appointment.getEndTime();
//            appointmentFromDb.setEndTime(endTime);
//        }
        String appointmentNumberStr = String.valueOf(appointment.getAppointmentNumber());
        if (!appointmentNumberStr.equals("0")) {
            appointmentFromDb.setAppointmentNumber(appointment.getAppointmentNumber());
        }
        // 第三步:保存数据
        appointmentDao.save(appointmentFromDb);
        // 返回结果
        return ResponseResult.SUCCESS("预约审批成功");
    }

    @Override
    public ResponseResult getAppointment(String appointmentId) {
        Appointment appointment = appointmentDao.findOneById(appointmentId);
        if (appointment == null) {
            return ResponseResult.FAILED("预约不存在");
        }
        return ResponseResult.SUCCESS("获取预约成功").setData(appointment);
    }

    @Override
    public ResponseResult listAppointment(int page, int size) {
        // 参数检查
        page = checkPage(page);
        size = checkSize(size);
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 创建分页条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        // 查询
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        // 返回结果
        final String userId = user.getId();
        Page<Appointment> all = appointmentDao.findAll(new Specification<Appointment>() {
            @Override
            public Predicate toPredicate(Root<Appointment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // 根据用户ID
                Predicate userIdPre = criteriaBuilder.equal(root.get("userId").as(String.class), userId);
                return criteriaBuilder.and(userIdPre);
            }
        }, pageable);
        return ResponseResult.SUCCESS("获取预约列表成功").setData(all);
    }
}
