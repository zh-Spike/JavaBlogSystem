package net.blog.services.impl;

import net.blog.dao.LoopDao;
import net.blog.pojo.Looper;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ILooperService;
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

@Service
@Transactional
public class LooperServiceImpl extends BaseService implements ILooperService {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private LoopDao loopDao;

    @Override
    public ResponseResult addLoop(Looper looper) {

        // 检查数据
        String title = looper.getTitle();
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("标题不能为空");
        }
        String imageUrl = looper.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            return ResponseResult.FAILED("图片不能为空");
        }
        String targetUrl = looper.getTargetUrl();
        if (TextUtils.isEmpty(targetUrl)) {
            return ResponseResult.FAILED("跳转链接不能为空");
        }
        // 补充数据
        looper.setId(idWorker.nextId() + "");
        looper.setCreateTime(new Date());
        looper.setUpdateTime(new Date());
        // 保存数据
        loopDao.save(looper);
        // 返回结果
        return ResponseResult.SUCCESS("添加成功");
    }

    @Override
    public ResponseResult getLoop(String loopId) {
        Looper looper = loopDao.findOneById(loopId);
        if (looper == null) {
            return ResponseResult.FAILED("轮播图不存在");
        }
        return ResponseResult.SUCCESS("轮播图获取成功").setData(looper);
    }

    @Autowired
    private IUserService userService;

    @Override
    public ResponseResult listLoops() {
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        User user = userService.checkUser();
        List<Looper> all;
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            // 只能获取正常的category
            all = loopDao.listLoopByState("1");
        } else {
            // 查询
            all = loopDao.findAll(sort);
        }
        return ResponseResult.SUCCESS("获取轮播图列表成功").setData(all);
    }


    @Override
    public ResponseResult updateLooper(String loopId, Looper looper) {
        // 查找所有数据
        Looper loopFromDb = loopDao.findOneById(loopId);
        if (loopFromDb == null) {
            return ResponseResult.FAILED("轮播图不存在");
        }
        // 判断数据 有的为非空
        // 可以为空的直接设置
        String title = looper.getTitle();
        if (!TextUtils.isEmpty(title)) {
            loopFromDb.setTitle(title);
        }
        String targetUrl = looper.getTargetUrl();
        if (!TextUtils.isEmpty(targetUrl)) {
            loopFromDb.setTargetUrl(targetUrl);
        }
        String imageUrl = looper.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            loopFromDb.setImageUrl(imageUrl);
        }
        String state = looper.getState();
        if (!TextUtils.isEmpty(state)) {
            loopFromDb.setState(state);
        }
        loopFromDb.setOrder(looper.getOrder());
        loopFromDb.setCreateTime(new Date());
        // 保存
        loopDao.save(loopFromDb);
        return ResponseResult.SUCCESS("轮播图保存成功");
    }

    @Override
    public ResponseResult deleteLooper(String loopId) {
        loopDao.deleteById(loopId);
        return ResponseResult.SUCCESS("删除成功");
    }


}
