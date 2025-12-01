package com.example.mall.common.exception;

import com.example.mall.common.api.IResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class CustomException extends RuntimeException {

    private final IResultCode resultCode;

    public CustomException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public CustomException(String message) {
        super(message);
        this.resultCode = null;
    }
}