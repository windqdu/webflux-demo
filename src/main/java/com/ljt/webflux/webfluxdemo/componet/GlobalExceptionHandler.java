package com.ljt.webflux.webfluxdemo.componet;

/**
 * @Description
 * @Author Fire
 * @Date 2020-01-19 16:56
 */

import com.ljt.webflux.webfluxdemo.model.ResponseVO;
import com.ljt.webflux.webfluxdemo.model.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public Object validationException(Exception e) {
        log.error(e.getMessage(),e);
        return new Exception("PARAM_IS_INVALID");
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error(e.getMessage(),e);

        ResponseVO result = new ResponseVO();
        result.setResultCode(ResultCode.ERROR.errorCode());
        result.setError(new ResponseVO.ErrorInfo(ResultCode.PARAM_IS_INVALID.errorCode(), e.getFieldError().getDefaultMessage()));
        log.debug("[Failed]Response is :{}", result.toString());

        return result;
    }

    @ExceptionHandler(Exception.class)
    public Object sysException(Exception e) {
        log.error(e.getMessage(),e);
        ResponseVO result = new ResponseVO();
        result.setResultCode(ResultCode.ERROR.errorCode());
        result.setError(new ResponseVO.ErrorInfo(ResultCode.PARAM_IS_INVALID.errorCode(), e.getLocalizedMessage()));
        log.debug("[Failed]Response is :{}", result.toString());
        return result;
    }
}
