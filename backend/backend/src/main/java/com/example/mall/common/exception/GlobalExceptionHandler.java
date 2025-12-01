package com.example.mall.common.exception;

import com.example.mall.common.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 返回400状态码
    public R<?> handleCustomException(CustomException e) {
        log.error("业务异常 -> {}", e.getMessage());
        if (e.getResultCode() != null) {
            return R.failed(e.getResultCode());
        }
        return R.failed(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("参数校验异常 -> {}", e.getMessage());
        // 获取第一个校验失败的字段信息
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return R.failed(message);
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 返回500状态码
    public R<?> handleGlobalException(Exception e) {
        log.error("服务器内部错误 -> ", e); // 打印完整堆栈信息
        return R.failed("服务器内部错误，请联系管理员");
    }
}