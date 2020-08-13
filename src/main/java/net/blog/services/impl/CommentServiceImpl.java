package net.blog.services.impl;

import net.blog.dao.ArticleNoContentDao;
import net.blog.dao.CommentDao;
import net.blog.pojo.ArticleNoContent;
import net.blog.pojo.Comment;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ICommentService;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

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
        // 返回结果
        return ResponseResult.SUCCESS("评论成功");
    }

    /**
     * 获取文章评论
     * TODO: 评论排序
     * 1. 时间 升序降序
     * 2. 置顶
     * 3. 后发表的 前单位时间排在前面 过了单位时间会按照点赞量和发表时间排序
     *
     * @param commentId
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listCommentByArticleId(String commentId, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Comment> all = commentDao.findAll(pageable);
        return ResponseResult.SUCCESS("评论列表获取成功").setData(all);
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
}
