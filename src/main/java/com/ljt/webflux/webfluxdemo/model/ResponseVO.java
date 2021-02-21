package com.ljt.webflux.webfluxdemo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO<T> {
    private Integer resultCode;
    private T data;
    private ErrorInfo error;

    public static ResponseVO success() {
        ResponseVO result = new ResponseVO();
        result.setResultCode(0);
        return result;
    }

    /**
     * 成功，创建ResResult：有data数据
     */
    public static ResponseVO success(Object data) {
        ResponseVO result = new ResponseVO();
        result.setResultCode(0);
        result.setData(data);
        log.debug("[Success]Response is :{}", result.toString());
        return result;
    }

    public static ResponseVO fail(String errorName) {
        ResponseVO result = new ResponseVO();
        result.setResultCode(1);
        result.setError(new ErrorInfo(10001, errorName));
        log.debug("[Failed]Response is :{}", result.toString());
        return result;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorInfo {
        private Integer errorCode;
        private String errorMsg;

    }
}
