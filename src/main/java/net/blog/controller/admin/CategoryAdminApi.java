package net.blog.controller.admin;

import net.blog.pojo.Category;
import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;

/**
 * 分类
 */
@RestController
@RequestMapping("/admin/category")
public class CategoryAdminApi {
    /**
     * 添加分类
     *
     */
    @PostMapping
    public ResponseResult addCategory(@RequestBody Category category){
        return null;
    }
    /**
     * 删除分类
     *
     */
    @DeleteMapping("/{categoryId}")
    public ResponseResult deleteCategory(@PathVariable("categoryId") String categoryId){
        return null;
    }
    /**
     * 更新分类
     *
     */
    @PutMapping
    public ResponseResult updateCategory(@PathVariable("categoryId") String categoryId,@RequestBody Category category){
        return null;
    }
    /**
     * 获取分类
     *
     */
    @GetMapping("/{categoryId}")
    public ResponseResult getCategory(@PathVariable("categoryId") String categoryId){
        return null;
    }
    /**
     * 分类列表
     *
     */
    @GetMapping("/list")
    public ResponseResult listCategories(@RequestParam("page")int page,@RequestParam("size")int size){
        return null;
    }

}
