package net.blog.services.impl;

import net.blog.dao.AppointmentDao;
import net.blog.pojo.Appointment;
import net.blog.pojo.Lab;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IAppointmentService;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;

public class AppointmentImpl extends BaseService implements IAppointmentService {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private AppointmentDao appointmentDao;

    @Override
    public ResponseResult addAppointment(Appointment appointment) {
        // 先检查数据
        if (TextUtils.isEmpty(appointment.getLabId())) {
            return ResponseResult.FAILED("实验室不可以为空");
        }
        // 判断实验室容量有点问题
        String appointmentNumberStr = String.valueOf(appointment.getAppointmentNumber());
        if (appointmentNumberStr.equals("0")) {
            return ResponseResult.FAILED("预约人数不能为空");
        }
        // 补全数据
        appointment.setId(idWorker.nextId() + "");
        appointment.setState("2");
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
            return ResponseResult.FAILED("该分类不存在");
        }
        return ResponseResult.SUCCESS("删除分类成功");
    }

    @Override
    public ResponseResult updateAppointment(String appointmentId, Appointment appointment) {
        // 第一步:找出
        Lab labFromDb = labDao.findOneById(labId);
        if (labFromDb == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        // 第二步：对内容进行判断，有些字段不能为空
        String name = lab.getLabName();
        if (!TextUtils.isEmpty(name)) {
            labFromDb.setLabName(name);
        }
        // 判断实验室容量有点问题
        String labNumberStrFromDb = String.valueOf(lab.getLabNumber());
        long labNumberFromDb = lab.getLabNumber();
        if (!labNumberStrFromDb.equals("0")) {
            labFromDb.setLabNumber(labNumberFromDb);
        }
        labFromDb.setState(lab.getState());
        labFromDb.setUpdateTime(new Date());
        // 第三步:保存数据
        labDao.save(labFromDb);
        // 返回结果
        return ResponseResult.SUCCESS("分类更新成功");
    }

    @Override
    public ResponseResult getAppointment(String appointmentId) {
        Lab lab = labDao.findOneById(labId);
        if (lab == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        return ResponseResult.SUCCESS("获取分类成功").setData(lab);
    }

    @Override
    public ResponseResult listComments(int page, int size) {
        // 参数检查
        // 创建条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        // 判断用户 普通/未登录用户  admin权限拉满
        User user = userService.checkUser();
        List<Lab> labs;
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            // 只能获取正常的lab
            labs = labDao.listLabByState("1");
        } else {
            // 查询
            labs = labDao.findAll(sort);
        }
        //返回结果
        return ResponseResult.SUCCESS("获取实验室列表成功").setData(labs);
    }

}
