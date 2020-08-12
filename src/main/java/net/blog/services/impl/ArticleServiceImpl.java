package net.blog.services.impl;

import net.blog.dao.ArticleDao;
import net.blog.dao.ArticleNoContentDao;
import net.blog.pojo.Article;
import net.blog.pojo.ArticleNoContent;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IArticleService;
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

@Service
@Transactional
public class ArticleServiceImpl extends BaseService implements IArticleService {

    @Autowired
    private IUserService userService;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleNoContentDao articleNoContentDao;

    /**
     * TODO: 1. 定时发布功能 2.多人博客审核 -- > 通知是否通过
     * <p>
     * 保存草稿
     * 1. 用户手动保存:页面跳转
     * 2. 自动保存:不发生页面跳转->多次提交->没唯一标识的话会重复添加到数据库里
     * <p>
     * 草稿都要有标题
     * 1. 每次发新文章时，向后台请求唯一ID,如果是更新的话不请求ID
     * 2. 可以直接提交，后台判断ID，没有就新建，返回此次返回。若有修改已存在内容
     * 手动保存:保存到后台
     * 自动保存:前端保存在本地
     *
     * <p>
     * 防止重复提交(网络卡顿时)
     * 1. 通过ID
     * 2. 通过token_key来判断,在30s内多次提交，只有最前的一次有效，其他return 提示不要频繁操作
     * <p>
     * 前端: 点击提交后，禁止按钮可使用，直到有响应结果再改变状态
     *
     * @param article
     * @return
     */
    @Override
    public ResponseResult postArticle(Article article) {
        // 检查用户
        User user = userService.checkUser();
        // 未登录
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 检查数据
        // title/分类ID/类型/摘要/标签
        String title = article.getTitle();
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("标题不能为空");
        }
        // 2种 草稿 发布
        String state = article.getState();
        if (!Constants.Article.STATE_PUBLISH.equals(state) &&
                !Constants.Article.STATE_DRAFT.equals(state)) {
            return ResponseResult.FAILED("不支持该操作");
        }
        String type = article.getType();
        if (TextUtils.isEmpty(type)) {
            return ResponseResult.FAILED("类型不能为空");
        }
        if (!"0".equals(type) && !"1".equals(type)) {
            return ResponseResult.FAILED("类型格式不匹配");
        }
        // 以下是发布的检查,草稿不需要检查
        if (Constants.Article.STATE_PUBLISH.equals(state)) {

            if (title.length() > Constants.Article.TITLE_MAX_LENGTH) {
                return ResponseResult.FAILED("文章标题不能超过" + Constants.Article.TITLE_MAX_LENGTH + "个字符");
            }

            String content = article.getContent();
            if (TextUtils.isEmpty(content)) {
                return ResponseResult.FAILED("内容不能为空");
            }
            String summary = article.getSummary();
            if (TextUtils.isEmpty(summary)) {
                return ResponseResult.FAILED("摘要不能为空");
            }
            if (summary.length() > Constants.Article.SUMMARY_MAX_LENGTH) {
                return ResponseResult.FAILED("摘要不能超过" + Constants.Article.SUMMARY_MAX_LENGTH + "个字符");
            }
            String labels = article.getLabels();
            // 标签-标签1-标签2
            if (TextUtils.isEmpty(labels)) {
                return ResponseResult.FAILED("标签不能为空");
            }
        }
        String artcleId = article.getId();
        if (TextUtils.isEmpty(artcleId)) {
            // 新内容 数据库里没的
            // 补充数据 ID/创建时间/更新时间/用户ID
            article.setId(idWorker.nextId() + "");
            article.setCreateTime(new Date());
        } else {
            // 更新内容 对已经发布的则不能再是草稿
            Article articleFromDb = articleDao.findOneById(artcleId);
            if (Constants.Article.STATE_PUBLISH.equals(articleFromDb.getState()) &&
                    Constants.Article.STATE_DRAFT.equals(state)) {
                // 已经发布了,只能更新，不能保存草稿
                return ResponseResult.FAILED("已发布文章不能保存成草稿");
            }
        }
        article.setUserId(user.getId());
        article.setUpdateTime(new Date());
        // 保存到数据库
        articleDao.save(article);
        // TODO: 保存到搜索的数据库
        // 返回,只有一种case才使用到ID
        // 如果使用到自动保存成草稿，就要加上ID，否则会创建多个Item
        return ResponseResult.SUCCESS(Constants.Article.STATE_DRAFT.equals(state) ? "草稿发布成功" :
                "文章发布成功").setData(article.getId());
    }

    /**
     * 管理中心 获取文章
     *
     * @param page       页码
     * @param size       每一页数量
     * @param state      状态:已删除 草稿 已发布 置顶
     * @param keyword    挑剔关键字 搜索
     * @param categoryId 分类ID
     * @return
     */
    @Override
    public ResponseResult listArticle(int page, int size, String state,
                                      String keyword, String categoryId) {
        // 处理一下size page
        page = checkPage(page);
        size = checkSize(size);
        // 创建分页和排序
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        // 开始查询
        Page<ArticleNoContent> all = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {
            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!TextUtils.isEmpty(state)) {
                    Predicate statePre = criteriaBuilder.equal(root.get("state").as(String.class), state);
                    predicates.add(statePre);
                }
                if (!TextUtils.isEmpty(categoryId)) {
                    Predicate categoryPre = criteriaBuilder.equal(root.get("categoryId").as(String.class), categoryId);
                    predicates.add(categoryPre);
                }
                if (!TextUtils.isEmpty(keyword)) {
                    Predicate titlePre = criteriaBuilder.like(root.get("title").as(String.class), "%" + keyword + "%");
                    predicates.add(titlePre);
                }
                Predicate[] preArray = new Predicate[predicates.size()];
                predicates.toArray(preArray);
                return criteriaBuilder.and(preArray);
            }
        }, pageable);
        // 处理查询条件
        return ResponseResult.SUCCESS("获取列表成功").setData(all);
    }
}
