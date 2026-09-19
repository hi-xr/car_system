package com.mszl.carsystem.handler;

import com.mszl.carsystem.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理：统一返回 Result，避免前端拿到非 JSON 导致页面异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSize(MaxUploadSizeExceededException e) {

        return Result.fail(413, "文件过大，请上传更小的图片");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("Unhandled exception", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "服务器异常";
        }
        return Result.fail(500, msg);
    }
}

