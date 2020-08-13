package net.blog.utils;

public interface Constants {

    int DEFAULT_SIZE = 30;

    interface User {
        String ROLE_ADMIN = "role_admin";
        String ROLE_NORMAL = "role_normal";
        String DEFAULT_AVATAR = "https://avatars0.githubusercontent.com/u/42293758?s=460&u=21b672fff6e347172b1df9d7ebf216e9c4c9c9fb&v=4";
        String DEFAULT_STATE = "1";
        String COOKIE_TOKEN_KEY = "blog_token";
        // redis的key
        String KEY_CAPTCHA_CONTENT = "key_captcha_content_";
        String KEY_EMAIL_CODE_CONTENT = "key_email_code_content_";
        String KEY_EMAIL_SEND_IP = "key_email_send_ip_";
        String KEY_EMAIL_SEND_ADDRESS = "key_email_send_address_";
        String KEY_TOKEN = "key_token_";
    }

    interface ImageType {
        String PREFIX = "image/";
        String TYPE_JPG = "jpg";
        String TYPE_PNG = "png";
        String TYPE_GIF = "gif";
        String TYPE_JPG_WITH_PREFIX = PREFIX + "jpeg";
        String TYPE_PNG_WITH_PREFIX = PREFIX + "png";
        String TYPE_GIF_WITH_PREFIX = PREFIX + "gif";
    }

    interface Settings {
        String MANAGER_ACCOUNT_INIT_STATE = "manager_account_init_state";
        String WEBSITE_TITLE = "website_title";
        String WEBSITE_DESCRIPTION = "website_description";
        String WEBSITE_KEYWORDS = "website_keywords";
        String WEBSITE_VIEW_COUNT = "website_view_count";
    }

    interface Page {
        int DEFAULT_PAGE = 1;
        int DEFAULT_SIZE = 10;
    }

    /**
     * 单位是秒
     */

    interface TimeValueInSecond {
        int MIN = 60;
        int HOUR = 60 * MIN;
        int HOUR_2 = 60 * MIN;
        int DAY = 24 * HOUR;
        int WEEK = 7 * DAY;
        int MONTH = 30 * DAY;
    }

    /**
     * 单位是毫秒
     */
    interface TimeValueInMillions {
        long MIN = 60 * 1000;
        long HOUR = 60 * MIN;
        long HOUR_2 = 60 * MIN;
        long DAY = 24 * HOUR;
        long WEEK = 7 * DAY;
        long MONTH = 30 * DAY;
    }

    interface Article {
        int TITLE_MAX_LENGTH = 128;
        int SUMMARY_MAX_LENGTH = 256;
        // 0删除 1正常发布 2草稿 3置顶
        String STATE_DELETE = "0";
        String STATE_PUBLISH = "1";
        String STATE_DRAFT = "2";
        String STATE_TOP = "3";
    }

    interface Comment {
        // 0删除 1正常发布 2草稿 3置顶
        String STATE_PUBLISH = "1";
        String STATE_TOP = "3";
    }


}
