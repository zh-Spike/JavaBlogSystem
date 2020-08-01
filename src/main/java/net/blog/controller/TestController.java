package net.blog.controller;

import net.blog.response.ResponeState;
import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

   @GetMapping("/Hello-world")
    public ResponseResult helloWorld() {
       System.out.println("Hello World!");
       ResponseResult responseResult = new ResponseResult(ResponeState.FAILED);
       responseResult.setData();
       return responseResult;
    }
}