package net.blog.services.impl;

import net.blog.pojo.User;
import net.blog.services.IUserService;
import net.blog.utils.Constants;
import net.blog.utils.CookieUtils;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service("permission")
public class PermissionService {

    @Autowired
    private IUserService userService;

    /**
     * 判断是不是管理员
     *
     * @return
     */
    public boolean admin() {
        // request and response
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return false;
        }

        User user = userService.checkUser();
        if (user == null) {
            return false;
        }
        if (Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            return true;
        }
        return false;
    }
}
