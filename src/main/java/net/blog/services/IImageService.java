package net.blog.services;

import net.blog.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IImageService {
    ResponseResult uploadImage(MultipartFile file);

    void viewImage(HttpServletResponse response, String imageId) throws IOException;

    ResponseResult listImages(int page, int size);

    ResponseResult deleteById(String imageId);
}
