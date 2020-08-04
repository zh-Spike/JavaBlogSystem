package net.blog.controller;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.LabelDao;
import net.blog.pojo.Labels;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Date;

@Transactional
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private LabelDao labelDao;

    @GetMapping("/Hello-world")
    public ResponseResult helloWorld() {
        log.info("Hello World!");
        return ResponseResult.SUCCESS().setData("Hello");
    }

    @PostMapping("/test-login")
    public ResponseResult testlogin(@RequestBody User user) {
        log.info("user name -== > " + user.getUser_name());
        log.info("password -== > " + user.getPassword());
        return ResponseResult.SUCCESS("登陆成功");
    }

    @PostMapping("/label")
    public ResponseResult addLabel(@RequestBody Labels labels) {
        //判断
        //补全
        labels.setId(idWorker.nextId() + "");
        labels.setCreateTime(new Date());
        labels.setUpdate_time(new Date());
        //保存
        labelDao.save(labels);
        return ResponseResult.SUCCESS("添加标签成功");
    }

    @DeleteMapping("/label/{labelId}")
    public ResponseResult delete(@PathVariable("labelId") String labelId){
        int deleteResult = labelDao.deleteOneById(labelId);
        log.info("deleteResult ==> " + deleteResult);
        if (deleteResult > 0){
        return ResponseResult.SUCCESS("删除标签成功");
        } else{
            return ResponseResult.FAILED("标签不存在");
        }
    }

    @PutMapping("/label/{labelId}")
    public ResponseResult updateLabel(@PathVariable("labelId")String labelId,@RequestBody Labels labels){
        Labels dbLabel = labelDao.findOneById(labelId);
        if (dbLabel == null) {
            return ResponseResult.FAILED("标签不存在");
        }
        dbLabel.setCount(labels.getCount());
        dbLabel.setName(labels.getName());
        dbLabel.setUpdate_time(new Date());
        labelDao.save(dbLabel);
        return ResponseResult.SUCCESS("修改成功");
    }

    @GetMapping("/label/{labelId}")
    public ResponseResult getLabelById(@PathVariable("labelId")String labelId){
        Labels dbLabel = labelDao.findOneById(labelId);
        if (dbLabel == null){
            return ResponseResult.FAILED("标签不存在");
        }
        return ResponseResult.SUCCESS("获取标签成功").setData(dbLabel);
    }

    @GetMapping("/label/list/{page}/{size}")
    public ResponseResult listLabels(@PathVariable("page")int page,@PathVariable("size") int size){
        if (page < 1) {
            page = 1;
        }
        if (size <= 0) {
            size = Constants.DEFAULT_SIZE;
        }
        Sort sort = new Sort(Sort.Direction.DESC,"createTime");
        Pageable pageable = PageRequest.of(page - 1, size,sort);
        Page<Labels> result = labelDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取成功").setData(result);
    }

}

