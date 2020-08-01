package net.blog.controller;

import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

   @GetMapping("/Hello-world")
    public ResponseResult helloWorld() {
       System.out.println("Hello World!");
       return ResponseResult.SUCCESS().setData("Hello");
    }

    @PostMapping("/test-login")
    public ResponseResult testlogin(@RequestBody User user){
       System.out.println("user name -== > "+user.getUser_name());
       System.out.println("password -== > "+user.getPassword());
       return ResponseResult.SUCCESS("登陆成功");
    }
}