package net.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

   @ResponseBody
   @RequestMapping(value = "/Hello-world",method = RequestMethod.GET)
    public String helloWorld() {
       System.out.println("Hello World!");
       return "hello world!";
    }
}