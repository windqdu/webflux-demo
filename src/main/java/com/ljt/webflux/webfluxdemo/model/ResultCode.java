package com.ljt.webflux.webfluxdemo.model;

public enum ResultCode {
    SUCCESS(0, "成功"),
    ERROR(1, "失败"),
    //系统级错误码
    SYSTEM_ERROR(10000, "系统异常，请稍后重试"),
    PARAM_NOT_EXISTS(10001, "参数缺失"),
    PARAM_IS_INVALID(10002, "参数无效"),
    ACCESSTOKE_IS_INVALID(10003, "用户认证失败"),
    INTERFACE_CALL_ERROR(10004, "第三方接口调用异常"),
    INTERFACE_RESULT_DATA_PARSE_ERROR(10005, "第三方接口返回数据解析异常"),

    //全局业务校验错误码
    FILE_SIZE_EXCEEDED(40001, "文件大小超限"),
    FILE_NOT_EXISTS(40002, "文件不存在"),
    BUINESS_PROESSING_EXCEPTION(40003, "业务处理异常"),
    BUINESS_DATA_NOT_EXISTS(40004, "业务数据不存在"),
    NO_OPERATION_PERMISSION(40005, "无操作权限"),

    //各业务处理错误码：（50--考试，51--知识课程,52--用户中心）
    CUST_EXAM_RELATION_NOT_EXISTS(50001, "考生不在当前考试学员范围内，不能参加考试"),
    EXAM_COMPLETED_BY_ADMIN(50002, "管理员已结束当前考试，考生不能再继续考试"),
    EXAM_STATUS_NOT_MATCH(50003, "考试状态不符合业务规则，操作失败"),
    LEARNING_NOTES_EXPORT_FAILED(51001, "学习笔记导出失败"),
    EXERCISE_RECORD_NOT_EXISTS(51002, "此用户练习记录不存在，请先登记练习"),
    COURSE_KLD_DELETE_FAILED(51003, "该课程/知识已被关联到其他的资源下，不允许被删除！"),
    SECTION_RESOURCE_EXISTS(51004, "章节下有资源，无法删除"),
    GAME_LEVEL_QUESTION_NUM_ERROR(60001, "题目数少于正确题目数");

    private Integer errorCode;
    private String errorMsg;

    ResultCode(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer errorCode() {
        return this.errorCode;
    }

    public String errorMsg() {
        return this.errorMsg;
    }

    public static String getErrorMessage(String name) {
        for (ResultCode item : ResultCode.values()) {
            if (item.name().equals(name)) {
                return item.errorMsg;
            }
        }
        return name;
    }

    public static Integer getErrorCode(String name) {
        for (ResultCode item : ResultCode.values()) {
            if (item.name().equals(name)) {
                return item.errorCode;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
