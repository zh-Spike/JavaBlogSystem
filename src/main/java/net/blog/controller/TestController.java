package net.blog.controller;

import lombok.extern.slf4j.Slf4j;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

   @GetMapping("/Hello-world")
    public ResponseResult helloWorld() {
       log.info("Hello World!");
       return ResponseResult.SUCCESS().setData("Hello");
    }

    @PostMapping("/test-login")
    public ResponseResult testlogin(@RequestBody User user){
       log.info("user name -== > "+user.getUser_name());
       log.info("password -== > "+user.getPassword());
       return ResponseResult.SUCCESS("登陆成功");
    }
}