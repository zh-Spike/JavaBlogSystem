package net.blog.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.ArticleNoContentDao;
import net.blog.dao.CommentDao;
import net.blog.pojo.ArticleNoContent;
import net.blog.pojo.Comment;
import net.blog.pojo.PageList;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ICommentService;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.RedisUtils;
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

@Slf4j
@Service
@Transactional
public class CommentServiceImpl extends BaseService implements ICommentService {
    @Autowired
    private IUserService userService;

    @Autowired
    private ArticleNoContentDao articleNoContentDao;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private CommentDao commentDao;

    /**
     * 发表评论
     *
     * @param comment
     * @return
     */
    @Override
    public ResponseResult postComment(Comment comment) {
        // 检查用户是否登录
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 检查内容
        String articleId = comment.getArticleId();
        if (TextUtils.isEmpty(articleId)) {
            return ResponseResult.FAILED("文章ID不能为空");
        }
        ArticleNoContent article = articleNoContentDao.findOneById(articleId);
        if (article == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        String content = comment.getContent();
        if (TextUtils.isEmpty(content)) {
            return ResponseResult.FAILED("评论内容不能为空");
        }
        // 补全内容
        comment.setId(idWorker.nextId() + "");
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        comment.setUserAvatar(user.getAvatar());
        comment.setUserName(user.getUserName());
        comment.setUserId(user.getId());
        // 保存入库
        commentDao.save(comment);
        // 清除对应文章的评论缓存
        redisUtils.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());
        // 返回结果
        return ResponseResult.SUCCESS("评论成功");
    }

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private Gson gson;

    /**
     * 获取文章评论
     * TODO: 评论排序
     * 1. 时间 升序降序
     * 2. 置顶
     * 3. 后发表的 前单位时间排在前面 过了单位时间会按照点赞量和发表时间排序
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listCommentByArticleId(String articleId, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        // 如果是第一页 我们先从缓存中返回
        // 如果时就返回
        String cacheJson = (String) redisUtils.get(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId);
        if (!TextUtils.isEmpty(cacheJson) && page == 1) {
            PageList<Comment> result = gson.fromJson(cacheJson, new TypeToken<PageList<Comment>>() {
            }.getType());
            log.info("comment list from redis");
            return ResponseResult.SUCCESS("评论列表获取成功").setData(result);
        }
        // 如果不是就继续
        Sort sort = new Sort(Sort.Direction.DESC, "state", "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAllByArticleId(articleId, pageable);
        // 把结果转成pageList
        PageList<Comment> result = new PageList<>();
        result.parsePage(all);
        // 保存一份到缓存
        if (page == 1) {
            redisUtils.set(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId, gson.toJson(result),
                    Constants.TimeValueInSecond.MIN_5);
        }
        return ResponseResult.SUCCESS("评论列表获取成功").setData(result);
    }

    @Override
    public ResponseResult deleteCommentById(String commentId) {
        // 判断用户角色
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 查找评论 对比用户权限
        Comment comment = commentDao.findOneById(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }
        // 用户ID不一样只有管理员才能删除
        // 如果用户ID一样 说明此评论是当前用户
        // 登录要判断角色
        if (user.getId().equals(commentId) ||
                Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            commentDao.deleteById(commentId);
            return ResponseResult.SUCCESS("评论删除成功");
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }
    }

    @Override
    public ResponseResult listComments(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取评论列表成功").setData(all);
    }

    @Override
    public ResponseResult topComment(String commentId) {
        Comment comment = commentDao.findOneById(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }
        String state = comment.getState();
        if (Constants.Comment.STATE_PUBLISH.equals(state)) {
            comment.setState(Constants.Comment.STATE_TOP);
            return ResponseResult.SUCCESS("置顶成功");
        } else if (Constants.Comment.STATE_TOP.equals(state)) {
            comment.setState(Constants.Comment.STATE_PUBLISH);
            return ResponseResult.SUCCESS("取消置顶成功");
        } else {
            return ResponseResult.FAILED("评论状态非法");
        }
    }

    @Override
    public ResponseResult getCommentCount() {
        long count = commentDao.count();
        return ResponseResult.SUCCESS("获取评论总量成功").setData(count);
    }
}
