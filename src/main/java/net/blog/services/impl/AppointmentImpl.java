package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.AppointmentDao;
import net.blog.dao.LabDao;
import net.blog.pojo.Appointment;
import net.blog.pojo.Lab;
import net.blog.pojo.PageList;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IAppointmentService;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AppointmentImpl extends BaseService implements IAppointmentService {

    @Autowired
    private IUserService userService;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private LabDao labDao;

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
        Lab labFormDb = labDao.findOneById(appointment.getLabId());
        if (labFormDb.getState().equals(Constants.Lab.STATE_DELETE)) {
            return ResponseResult.FAILED("当前实验室不可用");
        }
        // 判断实验室容量有点问题
        String appointmentNumberStr = String.valueOf(appointment.getAppointmentNumber());
        if (appointmentNumberStr.equals("0")) {
            return ResponseResult.FAILED("预约人数不能为空");
        } else if (appointment.getAppointmentNumber() > labFormDb.getLabAvailable()) {
            return ResponseResult.FAILED("该实验室可用容量不足");
        }
//        appointment.setStartTime(new Date(String.valueOf(appointment.getStartTime())));
//        appointment.setEndTime(new Date(String.valueOf(appointment.getEndTime())));
        // 补全数据
        appointment.setLabName(labFormDb.getLabName());
        appointment.setId(idWorker.nextId() + "");
        appointment.setState(Constants.Appointment.CHECKING);
        appointment.setIsUsed(Constants.Appointment.NOT_USED);
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
        return ResponseResult.SUCCESS("驳回申请");
    }

    @Override
    public ResponseResult updateAppointment(String appointmentId, Appointment appointment) {
        // 第一步:找出
        Appointment appointmentFromDb = appointmentDao.findOneById(appointmentId);
        if (appointmentFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        // 第二步：对内容进行判断，有些字段不能为空
        String state = appointment.getState();
        if (!TextUtils.isEmpty(state)) {
            appointmentFromDb.setState(state);
        }
        String labId = appointment.getLabId();
        if (!TextUtils.isEmpty(labId)) {
            appointmentFromDb.setLabId(labId);
            Lab newLabFromDb = labDao.findOneById(labId);
            appointmentFromDb.setLabName(newLabFromDb.getLabName());
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
        if (appointmentFromDb.getStartTime() != null) {
            appointmentFromDb.setStartTime(appointment.getStartTime());
        }
        if (appointmentFromDb.getEndTime() != null) {
            appointmentFromDb.setEndTime(appointment.getEndTime());
        }
        // 第三步:保存数据
        appointmentFromDb.setUpdateTime(new Date());
        appointmentDao.save(appointmentFromDb);
        // 返回结果
        return ResponseResult.SUCCESS("修改预约审批成功");
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
    public ResponseResult listAppointment(int page, int size, String userId, String state) {
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
        Page<Appointment> all = appointmentDao.findAll(new Specification<Appointment>() {
            @Override
            public Predicate toPredicate(Root<Appointment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!TextUtils.isEmpty(userId)) {
                    Predicate userIdPre = criteriaBuilder.equal(root.get("userId").as(String.class), userId);
                    predicates.add(userIdPre);
                }
                if (!TextUtils.isEmpty(state)) {
                    Predicate statePre = criteriaBuilder.equal(root.get("state").as(String.class), state);
                    predicates.add(statePre);
                }
                Predicate[] preArray = new Predicate[predicates.size()];
                predicates.toArray(preArray);
                return criteriaBuilder.and(preArray);
            }
        }, pageable);
        // 处理查询条件
        PageList<Appointment> result = new PageList<>();
        // 解析page
        result.parsePage(all);
        return ResponseResult.SUCCESS("获取预约列表成功").setData(all);
    }

    @Override
    public ResponseResult deleteAppointmentById(String appointmentId) {
        // 判断用户角色
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 查找评论 对比用户权限
        Appointment appointment = appointmentDao.findOneById(appointmentId);
        if (appointment == null) {
            return ResponseResult.FAILED("预约不存在");
        }
        // 用户ID不一样只有管理员才能删除
        // 如果用户ID一样 说明是当前用户
        // 登录要判断角色
        if (user.getId().equals(appointmentId) ||
                Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            appointmentDao.deleteById(appointmentId);
            return ResponseResult.SUCCESS("预约删除成功");
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }
    }

    @Override
    public ResponseResult checkAppointment(String appointmentId) {
        Appointment appointmentFromDb = appointmentDao.findOneById(appointmentId);
        if (appointmentFromDb == null) {
            return ResponseResult.FAILED("该预约不存在");
        }
        if (appointmentFromDb.getState().equals(Constants.Appointment.CHECKING)) {
            Lab labFromDb = labDao.findOneById(appointmentFromDb.getLabId());
            if (labFromDb.getLabAvailable() > appointmentFromDb.getAppointmentNumber()) {
                appointmentFromDb.setState(Constants.Appointment.PASSED);
                appointmentFromDb.setUpdateTime(new Date());
                appointmentDao.save(appointmentFromDb);
                return ResponseResult.SUCCESS("审批通过");
            } else {
                return ResponseResult.FAILED("实验室容量不足");
            }
        }
        appointmentFromDb.setUpdateTime(new Date());
        appointmentFromDb.setState(Constants.Appointment.CHECKING);
        appointmentDao.save(appointmentFromDb);
        return ResponseResult.SUCCESS("再给机会");
    }

    @Override
    public ResponseResult listUserAppointment() {
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        String userId = user.getId();
        // 创建分页条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        // 查询
        // 返回结果
        List<Appointment> all = appointmentDao.findAll(new Specification<Appointment>() {
            @Override
            public Predicate toPredicate(Root<Appointment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!TextUtils.isEmpty(userId)) {
                    Predicate userIdPre = criteriaBuilder.equal(root.get("userId").as(String.class), userId);
                    predicates.add(userIdPre);
                }
                Predicate[] preArray = new Predicate[predicates.size()];
                predicates.toArray(preArray);
                return criteriaBuilder.and(preArray);
            }
        }, sort);
        return ResponseResult.SUCCESS("获取预约列表成功").setData(all);
    }
}
