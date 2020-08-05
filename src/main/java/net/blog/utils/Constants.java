package net.blog.utils;

public interface Constants {

    int DEFAULT_SIZE = 30;

    interface User{
        String ROLE_ADMIN = "role_admin";
        String ROLE_NORMAL = "role_normal";
        String DEFAULT_AVATAR = "https://avatars0.githubusercontent.com/u/42293758?s=460&u=21b672fff6e347172b1df9d7ebf216e9c4c9c9fb&v=4";
        String DEFAULT_STATE ="1";
        String KEY_CAPTCHA_CONTENT = "key_captcha_content_";
        String KEY_EMAIL_CODE_CONTENT = "key_email_code_content_";
        String KEY_EMAIL_SEND_IP = "key_email_send_ip_";
        String KEY_EMAIL_SEND_ADDRESS = "key_email_send_address_";
    }

    interface Settings{
        String MANAGER_ACCOUNT_INIT_STATE ="mamger_account_init_state";
    }
}
