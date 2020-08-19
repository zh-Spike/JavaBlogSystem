package net.blog.controller.admin;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.response.ResponseResult;
import net.blog.services.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/image")
public class ImageAdminApi {

    @Autowired
    private IImageService imageService;

    /**
     * 关于图片(文件)上传
     * 1. 一般是对象存储-->看xx云文档
     * 2. 使用 Nginx + fastDFS ==> fastDFS 处理上传, Nginx 负责文件处理访问
     * 3.
     *
     * @param file
     * @return
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult uploadImage(@RequestParam("file") MultipartFile file) {
        return imageService.uploadImage(file);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId") String imageId) {
        return imageService.deleteById(imageId);
    }

//    @PreAuthorize("@permission.admin()")
//    @GetMapping("/{imageId}")
//    public void getImage(HttpServletResponse response,
//                         @PathVariable("imageId") String imageId) {
//        try {
//            imageService.viewImage(response, imageId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listImages(@PathVariable("page") int page, @PathVariable("size") int size) {
        return imageService.listImages(page, size);
    }

}
