package net.blog.services.impl;

import net.blog.dao.LoopDao;
import net.blog.pojo.Looper;
import net.blog.response.ResponseResult;
import net.blog.services.ILooperService;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

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

    @Override
    public ResponseResult listLoops(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        Pageable pageable = PageRequest.of(page -1,size,sort);
        Page<Looper> all = loopDao.findAll(pageable);
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
