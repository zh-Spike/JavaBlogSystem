package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 应用下载
 * 应用检查更新
 */
@RestController
@RequestMapping("/portal/app")
public class AppApi {

    /**
     * 给第三方扫描
     *
     * @return
     */
    // http://localhost:8081/portal/app/===745950517162672128
    @GetMapping("/{code}")
    public ResponseResult downloadAppForThirdScan(@PathVariable("code") String code,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        // TODO: 直接下载最新
        return null;
    }
}
