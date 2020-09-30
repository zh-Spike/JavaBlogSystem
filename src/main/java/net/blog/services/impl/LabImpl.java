package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.LabDao;
import net.blog.pojo.Lab;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ILabService;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
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
public class LabImpl extends BaseService implements ILabService {
    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private IUserService userService;

    @Autowired
    private LabDao labDao;

    @Override
    public ResponseResult addLab(Lab lab) {
        // 先检查数据
        if (TextUtils.isEmpty(lab.getLabName())) {
            return ResponseResult.FAILED("实验室名称不可以为空");
        }
        // 判断实验室容量有点问题
        String labNumberStr = String.valueOf(lab.getLabNumber());
        if (labNumberStr.equals("0")) {
            return ResponseResult.FAILED("实验室容量不能为空");
        }
        // 补全数据
        lab.setId(idWorker.nextId() + "");
        lab.setState("1");
        lab.setCreateTime(new Date());
        lab.setUpdateTime(new Date());
        lab.setLabAvailable(lab.getLabNumber());
        // 保存数据
        labDao.save(lab);
        // 返回结果
        return ResponseResult.SUCCESS("添加实验室信息成功");
    }

    @Override
    public ResponseResult deleteLab(String labId) {
        int result = labDao.deleteLabByUpdateState(labId);
        if (result == 0) {
            return ResponseResult.FAILED("该实验室信息不存在");
        }
        return ResponseResult.SUCCESS("删除实验室信息成功");
    }

    @Override
    public ResponseResult updateLab(String labId, Lab lab) {
        // 第一步:找出
        Lab labFromDb = labDao.findOneById(labId);
        if (labFromDb == null) {
            return ResponseResult.FAILED("该实验室不存在");
        }
        // 第二步：对内容进行判断，有些字段不能为空
        String name = lab.getLabName();
        if (!TextUtils.isEmpty(name)) {
            labFromDb.setLabName(name);
        }
        // 判断实验室容量有点问题
        String labNumberStr = String.valueOf(lab.getLabNumber());
        if (!labNumberStr.equals("0")) {
            labFromDb.setLabNumber(lab.getLabNumber());
        }
        if (!lab.getState().equals("0")) {
            labFromDb.setState(lab.getState());
        }
        if (!labNumberStr.equals("0")) {
            labFromDb.setLabAvailable(lab.getLabNumber());
        }
        labFromDb.setUpdateTime(new Date());
        // 第三步:保存数据
        labDao.save(labFromDb);
        // 返回结果
        return ResponseResult.SUCCESS("实验室信息更新成功");
    }

    @Override
    public ResponseResult getLab(String labId) {
        Lab lab = labDao.findOneById(labId);
        if (lab == null) {
            return ResponseResult.FAILED("该实验室信息不存在");
        }
        return ResponseResult.SUCCESS("获取实验室信息成功").setData(lab);
    }

    @Override
    public ResponseResult listLab() {
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

    @Override
    public void updateAvailableNumber(String labId) {
        Lab labFromDb = labDao.findOneById(labId);

    }

}
