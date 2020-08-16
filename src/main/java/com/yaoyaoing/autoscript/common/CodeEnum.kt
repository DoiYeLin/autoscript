package com.yaoyaoing.autoscript.common

/**
 * 返回前端公用code码
 */
enum class CodeEnum(var code: Int, var msg: String) {
    SUCCESS(200, "成功"),
    ERROR(100, "未知"),
    WARNING(101, "警告"),
    USERKEY_EMPTY(102, "userKey is empty"),
    App_Version(103, "plass App"),
    NOT_CONTENT(110, "返回内容为空"),
    token_error(201, "Token错误，请刷新重试"),
    ACCESS_REFULE(301, "访问被拒绝"),
    SERVER_ERROR(-1, "服务错误"),
    REQUEST_METHOD_ERROR(-3, "请求方法错误"),
    REQUEST_PARAM_ERROR(-4, "请求参数错误"),
    REQUEST_JSON_FORMAT_ERR(-5, "请求json格式错误"),
    NOCOMPETENCE(-8, "无权限访问"),
    LOGIN_FAIL(-9, "账号或密码错误"),
    NO_LOGIN(-10, "Token失效或无登录状态"),
    ACCOUNT_NOT_EXIST(-11, "账号不存在"),
    DATA_INVALID(-12, "存在不合法数据"),
    USER_NOT_ENABLE(-13, "用户已禁用"),
    NOTHING_DELETE(-14, "删除暂存信息失败,未找到匹配内容"),
    USER_EXIST(-15, "用户已存在"),
    OLD_PWD_ERROR(-16, "旧密码错误"),
    PWD_FORMAT_ERROR(-17, "密码长度最少6位"),
    FILE_EXIST(-18, "文件已存在"),
    MAC_FORBID_LOGIN(-19, "当前机器禁止登陆"),
    INPUT_SMS_CODE(-20, "请输入短信验证码"),
    SMS_CODE_ERROR(-21, "短信验证码错误"),
    FILE_FAIL(-22, "文件解析失败"),
    NOT_FOUND_QRCODE(-23, "未找到二维码"),
    REQUEST_FREQUENT(-24, "请求太频繁"),
    SMS_CODE_SEDN_FAIL(-25, "验证码发送失败,请稍后重试"),
    PHONE_NUM_ERROR(-26, "手机号格式错误"),
    PHONE_NUM_IS_NULL(-28, "手机号不可为空"),
    PASSWORD_IS_NULL(-29, "密码不可为空");

}