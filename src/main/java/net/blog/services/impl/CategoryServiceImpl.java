package net.blog.services.impl;

import net.blog.dao.CategoryDao;
import net.blog.pojo.Category;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.ICategoryService;
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
public class CategoryServiceImpl extends BaseService implements ICategoryService {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private IUserService userService;

    @Override
    public ResponseResult addCategory(Category category) {
        // 先检查数据
        // 必须的数据：
        // 名称、分类的pinyin、顺序、描述
        if (TextUtils.isEmpty(category.getName())) {
            return ResponseResult.FAILED("分类名称不可以为空");
        }
        if (TextUtils.isEmpty(category.getPinyin())) {
            return ResponseResult.FAILED("分类拼音不可以为空");
        }
        if (TextUtils.isEmpty(category.getDescription())) {
            return ResponseResult.FAILED("分类描述不可以为空");
        }
        // 补全数据
        category.setId(idWorker.nextId() + "");
        category.setStatus("1");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        // 保存数据
        categoryDao.save(category);
        // 返回结果
        return ResponseResult.SUCCESS("添加分类成功");
    }

    @Override
    public ResponseResult getCategory(String categoryId) {
        Category category = categoryDao.findOneById(categoryId);
        if (category == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        return ResponseResult.SUCCESS("获取分类成功").setData(category);
    }

    @Override
    public ResponseResult listCategories() {
        // 参数检查
        // 创建条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime", "order");
        // 判断用户 普通/未登录用户  admin权限拉满
        User user = userService.checkUser();
        List<Category> categories;
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            // 只能获取正常的category
            categories = categoryDao.listCategoryByState("1");
        } else {
            // 查询
            categories = categoryDao.findAll(sort);
        }
        //返回结果
        return ResponseResult.SUCCESS("获取分类列表成功").setData(categories);
    }

    @Override
    public ResponseResult updateCategory(String categoryId, Category category) {
        // 第一步:找出
        Category categoryFromDb = categoryDao.findOneById(categoryId);
        if (categoryFromDb == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        // 第二步：对内容进行判断，有些字段不能为空
        String name = category.getName();
        if (!TextUtils.isEmpty(name)) {
            categoryFromDb.setName(name);
        }
        String pinyin = category.getPinyin();
        if (!TextUtils.isEmpty(pinyin)) {
            categoryFromDb.setPinyin(pinyin);
        }
        String description = category.getDescription();
        if (!TextUtils.isEmpty(description)) {
            categoryFromDb.setDescription(description);
        }
        categoryFromDb.setStatus(category.getStatus());
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setUpdateTime(new Date());
        // 第三步:保存数据
        categoryDao.save(categoryFromDb);
        // 返回结果
        return ResponseResult.SUCCESS("分类更新成功");
    }

    @Override
    public ResponseResult deleteCategory(String categoryId) {
        int result = categoryDao.deleteCategoriesByUpdateState(categoryId);
        if (result == 0) {
            return ResponseResult.FAILED("该分类不存在");
        }
        return ResponseResult.SUCCESS("删除分类成功");
    }
}

