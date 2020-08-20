package net.blog.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static final String regEx = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    public static boolean isEmailAddressRight(String emailAddress) {
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(emailAddress);
        return m.matches();
    }

    public static String getDomain(HttpServletRequest request){
        StringBuffer requestURL = request.getRequestURL();
        String servletPath = request.getServletPath();
        return requestURL.toString().replace(servletPath, "");
    }
}
