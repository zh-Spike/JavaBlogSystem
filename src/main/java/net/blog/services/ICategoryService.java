package net.blog.services;

import net.blog.pojo.Category;
import net.blog.response.ResponseResult;


public interface ICategoryService {
    ResponseResult addCategory(Category category);

    ResponseResult getCategory(String categoryId);

    ResponseResult listCategories();

    ResponseResult updateCategory(String categoryId, Category category);

    ResponseResult deleteCategory(String categoryId);
}
