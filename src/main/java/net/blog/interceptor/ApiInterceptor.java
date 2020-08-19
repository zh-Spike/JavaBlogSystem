package net.blog.interceptor;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.blog.response.ResponseResult;
import net.blog.utils.Constants;
import net.blog.utils.CookieUtils;
import net.blog.utils.RedisUtils;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

@Component
@Slf4j
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 不是所有都要拦截
        // 有些需要的拦截
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            String name = method.getName();
            log.info("method name == > " + name);
            CheckTooFrequentCommit methodAnnotation = handlerMethod.getMethodAnnotation(CheckTooFrequentCommit.class);
            if (methodAnnotation != null) {
                String methodName = handlerMethod.getMethod().getName();
                // 所有提交内容的方法必须是用户登录的 用token作为key来记录请求频率
                String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
                log.info("tokenKey -||- >" + tokenKey);
                if (!TextUtils.isEmpty(tokenKey)) {
                    String hasCommit = (String) redisUtils.get(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey + methodName);
                    if (!TextUtils.isEmpty(hasCommit)) {
                        // 从redis里读取 判断是否存在 如果存在则返回提交太频繁
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        ResponseResult failed = ResponseResult.FAILED("提交过于频繁,请稍后重试");
                        PrintWriter writer = response.getWriter();
                        writer.write(gson.toJson(failed));
                        writer.flush();
                        return false;
                    } else {
                        // 如果不存在则可以提交并记录此次提交 有效期为10s
                        redisUtils.set(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey,
                                "true", Constants.TimeValueInSecond.SECOND_10);
                    }

                }

                // 判断是否提交太频繁了
                log.info("check commit too frequent");
            }
        }

        // ture 放行
        // false 拦截
        return true;
    }
}
