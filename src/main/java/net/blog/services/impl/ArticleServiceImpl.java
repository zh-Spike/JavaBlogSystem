package net.blog.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import net.blog.dao.ArticleDao;
import net.blog.dao.ArticleNoContentDao;
import net.blog.dao.CommentDao;
import net.blog.dao.LabelDao;
import net.blog.pojo.*;
import net.blog.response.ResponseResult;
import net.blog.services.IArticleService;
import net.blog.services.ISolrService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.*;

@Slf4j
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

    @Autowired
    private ISolrService solrService;

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
            String labels = article.getLabel();
            // 标签-标签1-标签2
            if (TextUtils.isEmpty(labels)) {
                return ResponseResult.FAILED("标签不能为空");
            }
        }
        String articleId = article.getId();
        if (TextUtils.isEmpty(articleId)) {
            // 新内容 数据库里没的
            // 补充数据 ID/创建时间/更新时间/用户ID
            article.setId(idWorker.nextId() + "");
            article.setCreateTime(new Date());
        } else {
            // 更新内容 对已经发布的则不能再是草稿
            Article articleFromDb = articleDao.findOneById(articleId);
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
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            // 保存到搜索的数据库 只有正式发布才能记录到数据库
            solrService.addArticle(article);
        }
        // 打散标签 入库 统计
        this.setLabels(article.getLabel());
        // 删除文章列表
        redisUtils.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
        // 返回,只有一种case才使用到ID
        // 如果使用到自动保存成草稿，就要加上ID，否则会创建多个Item
        return ResponseResult.SUCCESS(Constants.Article.STATE_DRAFT.equals(state) ? "草稿更新成功" :
                "文章发布成功").setData(article.getId());
    }

    @Autowired
    private LabelDao labelDao;

    private void setLabels(String labels) {
        List<String> labelList = new ArrayList<>();
        if (labels.contains("-")) {
            labelList.addAll(Arrays.asList(labels.split("-")));
        } else {
            labelList.add(labels);
        }
        // 入库 统计
        for (String label : labelList) {
            // 找出来
//            Labels targetLabel = labelDao.findOneByName(label);
//            if (targetLabel == null) {
//                targetLabel = new Labels();
//                targetLabel.setId(idWorker.nextId()+"");
//                targetLabel.setCount(0);
//                targetLabel.setName(label);
//                targetLabel.setCreateTime(new Date());
//            }
//            long count = targetLabel.getCount();
//            targetLabel.setCount(++count);
//            targetLabel.setUpdateTime(new Date());
            int result = labelDao.updateCountByName(label);
            if (result == 0) {
                Labels targetLabel = new Labels();
                targetLabel.setId(idWorker.nextId() + "");
                targetLabel.setCount(1);
                targetLabel.setName(label);
                targetLabel.setCreateTime(new Date());
                targetLabel.setUpdateTime(new Date());
                labelDao.save(targetLabel);
            }
        }
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
    public ResponseResult listArticles(int page, int size,
                                       String keyword, String categoryId, String state) {
        // 处理一下size page
        page = checkPage(page);
        size = checkSize(size);
        // 第一页内容做缓存
        String articleListJson = (String) redisUtils.get(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
        boolean isSearch = !TextUtils.isEmpty(keyword) || !TextUtils.isEmpty(categoryId) || !TextUtils.isEmpty(state);
        if (!TextUtils.isEmpty(articleListJson) && page == 1 && !isSearch) {
            PageList<ArticleNoContent> result = gson.fromJson(articleListJson, new TypeToken<PageList<ArticleNoContent>>() {
            }.getType());
            log.info("article list first page from redis");
            return ResponseResult.SUCCESS("获取文章列表成功").setData(result);
        }
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
        PageList<ArticleNoContent> result = new PageList<>();
        // 解析page
        result.parsePage(all);
        // 保存到redis
        if (page == 1 && !isSearch) {
            redisUtils.set(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE, gson.toJson(result), Constants.TimeValueInSecond.MIN_15);
        }
        return ResponseResult.SUCCESS("获取列表成功").setData(result);
    }

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private Gson gson;

    /**
     * 如果有审核机制 审核的文章只能管理员和作者自己才能看
     * 草稿、删除、置顶、发布
     * 删除的不能获取,其他都可以
     * <p>
     * 统计文章阅读量
     * 精确:对IP进行处理 如果是同个IP则不保存
     * <p>
     * 先把阅读量保存到redis里更新
     * 文章也在redis里缓存一份 10 minutes
     * 当文章消失时 从mysql中提取,同时更新阅读量
     * 10分钟后在下一次访问更新阅读量
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult getArticleById(String articleId) {
        // 先从redis里获取
        // 如果没有再从mysql
        String articleJson = (String) redisUtils.get(Constants.Article.KEY_ARTICLE_CACHE + articleId);
        if (!TextUtils.isEmpty(articleJson)) {
            log.info("article detail from redis");
            Article article = gson.fromJson(articleJson, Article.class);
            // 增加阅读数量
            redisUtils.incr(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, 1);
            return ResponseResult.SUCCESS("获取文章成功").setData(article);
        }
        // 查询文章
        Article article = articleDao.findOneById(articleId);
        if (article == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        // 判断文章状态
        String state = article.getState();
        if (Constants.Article.STATE_PUBLISH.equals(state) ||
                Constants.Article.STATE_TOP.equals(state)) {
            // 处理文章内容
            String html;
            // 转HTML
            if (Constants.Article.TYPE_MARKDOWN.equals(article.getType())) {
                // 转成html
                // markdown to html
                MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        JekyllTagExtension.create(),
                        TocExtension.create(),
                        SimTocExtension.create()
                ));
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(article.getContent());
                html = renderer.render(document);
            } else {
                html = article.getContent();
            }
            // 搞一份文章的复制
            String articleGsonCopy = gson.toJson(article);
            Article newArticle = gson.fromJson(articleGsonCopy, Article.class);
            newArticle.setContent(html);
            // 正常发布才能增加阅读量
            redisUtils.set(Constants.Article.KEY_ARTICLE_CACHE + articleId,
                    gson.toJson(newArticle), Constants.TimeValueInSecond.MIN_5);
            // 设置阅读量的key 先从redis里获取,如果redis里没有就从article中获取,并且再添加到redis里
            String viewCount = (String) redisUtils.get(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId);
            if (TextUtils.isEmpty(viewCount)) {
                long newCount = article.getViewCount() + 1;
                redisUtils.set(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, String.valueOf(newCount));
            } else {
                // 有的话就更新到mysql里
                long newCount = redisUtils.incr(Constants.Article.KEY_ARTICLE_VIEW_COUNT + articleId, 1);
                article.setViewCount(newCount);
                articleDao.save(article);
                // 更新solr里的阅读量
                solrService.updateArticle(articleId, article);
            }
            // 可以返回
            return ResponseResult.SUCCESS("获取文章成功").setData(newArticle);
        }
        // 如果删除/草稿,需要admin
        User user = userService.checkUser();
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            return ResponseResult.PERMISSION_DENIED();
        }
        // 返回结果
        return ResponseResult.SUCCESS("获取文章成功").setData(article);
    }

    /**
     * 更新文章内容
     * 只支持修改 标题 内容 标签 分类 摘要
     *
     * @param articleId 更新文章的ID
     * @param article   文章
     * @return
     */
    @Override
    public ResponseResult updateArticle(String articleId, Article article) {
        // 找出来
        Article articleFromDb = articleDao.findOneById(articleId);
        if (articleFromDb == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        // 内容修改
        String title = article.getTitle();
        if (!TextUtils.isEmpty(title)) {
            articleFromDb.setTitle(title);
        }
        String summary = article.getSummary();
        if (!TextUtils.isEmpty(summary)) {
            articleFromDb.setSummary(summary);
        }
        String content = article.getContent();
        if (!TextUtils.isEmpty(content)) {
            articleFromDb.setContent(content);
        }
        String label = article.getLabel();
        if (!TextUtils.isEmpty(label)) {
            articleFromDb.setLabel(label);
        }
        String state = article.getState();
        if (!TextUtils.isEmpty(state)) {
            articleFromDb.setState(state);
        }
        String categoryId = article.getCategoryId();
        if (!TextUtils.isEmpty(categoryId)) {
            articleFromDb.setCategoryId(categoryId);
        }
        articleFromDb.setCover(article.getCover());
        articleFromDb.setUpdateTime(new Date());
        articleDao.save(articleFromDb);
        redisUtils.del(Constants.Article.KEY_ARTICLE_CACHE + articleId, Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
        // 返回接口
        return ResponseResult.SUCCESS("文章更新成功");
    }

    @Autowired
    private CommentDao commentDao;

    /**
     * 删除 物理
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult deleteArticleById(String articleId) {
        // 先删除评论 评论的articleId,外键是article的Id
        commentDao.deleteAllByArticleId(articleId);
        int result = articleDao.deleteAllById(articleId);
        if (result > 0) {
            redisUtils.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
            redisUtils.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            // 删除搜索库中的内容
            solrService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("文章删除成功");
        }
        return ResponseResult.FAILED("文章不存在");
    }

    /**
     * 通过修改状态删除 标记删除
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult deleteArticleByState(String articleId) {
        int result = articleDao.deleteArticleByState(articleId);
        if (result > 0) {
            redisUtils.del(Constants.Article.KEY_ARTICLE_CACHE + articleId);
            redisUtils.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            // 删除搜索库中的内容
            solrService.deleteArticle(articleId);
            return ResponseResult.SUCCESS("文章删除成功");
        }
        return ResponseResult.FAILED("文章不存在");
    }

    @Override
    public ResponseResult topArticle(String articleId) {
        // 必须是已经发布的才能置顶
        Article article = articleDao.findOneById(articleId);
        if (article == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        String state = article.getState();
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            article.setState(Constants.Article.STATE_TOP);
            articleDao.save(article);
            redisUtils.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            return ResponseResult.SUCCESS("置顶成功");
        }
        if (Constants.Article.STATE_TOP.equals(state)) {
            article.setState(Constants.Article.STATE_PUBLISH);
            articleDao.save(article);
            redisUtils.del(Constants.Article.KEY_ARTICLE_LIST_FIRST_PAGE);
            return ResponseResult.SUCCESS("取消置顶");
        }
        return ResponseResult.FAILED("不支持该操作");
    }

    /**
     * 获取置顶文章
     * 与权限无关
     * 状态必须置顶
     *
     * @return
     */
    @Override
    public ResponseResult listTopArticles() {
        List<ArticleNoContent> result = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {
            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("state").as(String.class), Constants.Article.STATE_TOP);
            }
        });
        return ResponseResult.SUCCESS("获取置顶文章成功").setData(result);
    }

    @Autowired
    private Random random;

    /**
     * 获取推荐文章 通过标签来计算
     *
     * @param articleId
     * @param size
     * @return
     */
    @Override
    public ResponseResult listRecommendArticle(String articleId, int size) {
        // 查询文章 不需要文章内容 只要标签
        String labels = articleDao.listArticleLabelsById(articleId);
        // 分割标签
        List<String> labelList = new ArrayList<>();
        if (!labels.contains("-")) {
            labelList.add(labels);
        } else {
            labelList.addAll(Arrays.asList(labels.split("-")));
        }
        // 从列表中随机获取标签来查询相似的内容
        String targetLabel = labelList.get(random.nextInt(labelList.size()));
        log.info("targetLabel == >" + targetLabel);
        List<ArticleNoContent> likeResultList = articleNoContentDao.listArticleByLikeLabel("%" + targetLabel + "%", articleId, size);
        // 判断长度
        if (likeResultList.size() < size) {
            // 说明长度不够,用最新文章补充
            int dxSize = size - likeResultList.size();
            List<ArticleNoContent> dxList = articleNoContentDao.listLastArticleBySize(articleId, dxSize);
            // 会把前面找到的也找进来
            likeResultList.addAll(dxList);
        }
        return ResponseResult.SUCCESS("获取推荐文章成功").setData(likeResultList);
    }

    @Override
    public ResponseResult listArticlesByLabel(int page, int size, String label) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        PageRequest pageable = PageRequest.of(page - 1, size, sort);
        Page<ArticleNoContent> all = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {
            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate labelPre = criteriaBuilder.like(root.get("labels").as(String.class), "%" + label + "%");
                Predicate statePublishPre = criteriaBuilder.equal(root.get("state").as(String.class), Constants.Article.STATE_PUBLISH);
                Predicate statePublishTopPre = criteriaBuilder.equal(root.get("state").as(String.class), Constants.Article.STATE_TOP);
                Predicate or = criteriaBuilder.or(statePublishPre, statePublishTopPre);
                return criteriaBuilder.and(or, labelPre);
            }
        }, pageable);
        return ResponseResult.SUCCESS("获取文章列表成功").setData(all);
    }

    @Override
    public ResponseResult listLabels(int size) {
        size = this.checkSize(size);
        Sort sort = new Sort(Sort.Direction.DESC, "count");
        Pageable pageable = PageRequest.of(0, size, sort);
        Page<Labels> all = labelDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取标签成功").setData(all);
    }

    @Override
    public ResponseResult getArticleCount() {
        long count = articleDao.count();
        return ResponseResult.SUCCESS("获取文章总量成功").setData(count);
    }
}
