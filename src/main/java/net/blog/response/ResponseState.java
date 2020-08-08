package net.blog.response;

public enum ResponseState {
    SUCCESS(10000, true, "操作成功"),
    JOIN_IN_SUCCESS(60001, true, "注册成功"),
    FAILED(20000, false, "操作失败"),
    PARAMS_ILL(30000, false, "参数错误"),
    ACCOUNT_NOT_LOGIN(40002, false, "账号未登录"),
    PERMISSION_DENIED(40003, false, "权限不够"),
    ACCOUNT_DENIED(40004, false, "账号禁止"),
    LOGIN_SUCCESS(60000, true, "登录成功");

    ResponseState(int code, boolean isSuccess, String message) {
        this.code = code;
        this.isSuccess = isSuccess;
        this.message = message;
    }

    private int code;
    private boolean isSuccess;
    private String message;


    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getCode() {
        return code;
    }

}