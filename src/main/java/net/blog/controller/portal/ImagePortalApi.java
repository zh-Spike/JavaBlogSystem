package net.blog.controller.portal;

import net.blog.services.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/portal/image")
public class ImagePortalApi {

    @Autowired
    private IImageService imageService;

    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response,
                         @PathVariable("imageId") String imageId) {
        try {
            imageService.viewImage(response, imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
