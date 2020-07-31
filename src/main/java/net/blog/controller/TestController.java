package net.blog.controller;

import net.blog.response.ResponseResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

   @ResponseBody
   @RequestMapping(value = "/Hello-world",method = RequestMethod.GET)
    public ResponseResult helloWorld() {
       System.out.println("Hello World!");
       ResponseResult responseResult = new ResponseResult();
       responseResult.setSuccess(true);
       responseResult.setCode(20000);
       responseResult.setMessage("操作成功");
       responseResult.setData("hello world");
       return responseResult;
    }
}