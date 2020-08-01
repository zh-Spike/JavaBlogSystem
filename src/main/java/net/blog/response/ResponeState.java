package net.blog.response;

public enum ResponseState implements ICommentResult {
    SUCCESS(10000, true, "操作成功"),
    FAILED(20000, false, "操作失败"),
    PARAMS_ILL(30000, false, "参数错误"),
    PERMISSION_DENIED(40000, false, "权限不够"),
    NOT_LOGIN(50000, false, "账号未登录")，
    LOGIN_SUCCESS(60000,true, "登录成功");


    int code;
    boolean isSuccess;
    String message;

    ResponseState(int code, boolean isSuccess, String message) {
        this.code = code;
        this.isSuccess = isSuccess;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public int getCode() {
        return code;
    }

}