package net.blog.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.CommentDao;
import net.blog.dao.LabelDao;
import net.blog.pojo.Comment;
import net.blog.pojo.Labels;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.CookieUtils;
import net.blog.utils.RedisUtils;
import net.blog.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Transactional
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private LabelDao labelDao;
    private String tokenKey;

    @GetMapping("/Hello-world")
    public ResponseResult helloWorld() {
        log.info("Hello World!");
        String captchaContent = (String) redisUtils.get(Constants.User.KEY_CAPTCHA_CONTENT + "123456");
        log.info("capchaContent == >" + captchaContent);
        return ResponseResult.SUCCESS().setData("Hello");
    }

    @PostMapping("/test-login")
    public ResponseResult testlogin(@RequestBody User user) {
        log.info("user name -== > " + user.getUserName());
        log.info("password -== > " + user.getPassword());
        return ResponseResult.SUCCESS("登陆成功");
    }

    @PostMapping("/label")
    public ResponseResult addLabel(@RequestBody Labels labels) {
        //判断
        //补全
        labels.setId(idWorker.nextId() + "");
        labels.setCreateTime(new Date());
        labels.setUpdateTime(new Date());
        //保存
        labelDao.save(labels);
        return ResponseResult.SUCCESS("添加标签成功");
    }

    @DeleteMapping("/label/{labelId}")
    public ResponseResult delete(@PathVariable("labelId") String labelId) {
        int deleteResult = labelDao.deleteOneById(labelId);
        log.info("deleteResult ==> " + deleteResult);
        if (deleteResult > 0) {
            return ResponseResult.SUCCESS("删除标签成功");
        } else {
            return ResponseResult.FAILED("标签不存在");
        }
    }

    @PutMapping("/label/{labelId}")
    public ResponseResult updateLabel(@PathVariable("labelId") String labelId, @RequestBody Labels labels) {
        Labels dbLabel = labelDao.findOneById(labelId);
        if (dbLabel == null) {
            return ResponseResult.FAILED("标签不存在");
        }
        dbLabel.setCount(labels.getCount());
        dbLabel.setName(labels.getName());
        dbLabel.setUpdateTime(new Date());
        labelDao.save(dbLabel);
        return ResponseResult.SUCCESS("修改成功");
    }

    @GetMapping("/label/{labelId}")
    public ResponseResult getLabelById(@PathVariable("labelId") String labelId) {
        Labels dbLabel = labelDao.findOneById(labelId);
        if (dbLabel == null) {
            return ResponseResult.FAILED("标签不存在");
        }
        return ResponseResult.SUCCESS("获取标签成功").setData(dbLabel);
    }

    @GetMapping("/label/list/{page}/{size}")
    public ResponseResult listLabels(@PathVariable("page") int page, @PathVariable("size") int size) {
        if (page < 1) {
            page = 1;
        }
        if (size <= 0) {
            size = Constants.DEFAULT_SIZE;
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Labels> result = labelDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取成功").setData(result);
    }

    @GetMapping("/label/search")
    public ResponseResult doLabelSearch(@RequestParam("keyword") String keyword, @RequestParam("count") int count) {
//        Labels OneByName = labelDao.findOneByName(keyword);
//        return ResponseResult.SUCCESS("查找成功").setData(OneByName);
        List<Labels> all = labelDao.findAll(new Specification<Labels>() {
            @Override
            public Predicate toPredicate(Root<Labels> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate namePre = criteriaBuilder.like(root.get("name").as(String.class), "%" + keyword + "%");
                Predicate countPre = criteriaBuilder.equal(root.get("count").as(Integer.class), count);
                Predicate and = criteriaBuilder.and(namePre, countPre);
                return and;
            }
        });
        if (all.size() == 0) {
            return ResponseResult.FAILED("结果为空");
        }
        return ResponseResult.SUCCESS("查找成功").setData(all);
    }

    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        // specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        specCaptcha.setFont(Captcha.FONT_1);
        // 设置类型，纯数字、纯字母、字母数字混合
        //specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String content = specCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);
        // 验证码存入session
        //request.getSession().setAttribute("captcha", content);
        //存入redis,10分钟
        redisUtils.set(Constants.User.KEY_CAPTCHA_CONTENT + "123456", content, 60 * 10);
        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private IUserService userService;

    @PostMapping("/comment")
    public ResponseResult testComment(@RequestBody Comment comment,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        String content = comment.getContent();
        log.info("comment content == >" + content);
        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
        if (tokenKey == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }

        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        comment.setUserId(user.getId());
        comment.setUserAvatar(user.getAvatar());
        comment.setUserName(user.getUserName());
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setId(idWorker.nextId() + "");
        commentDao.save(comment);
        return ResponseResult.SUCCESS("评论成功");
    }
}


