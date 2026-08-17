package com.athena.common.exception;

import com.athena.common.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常处理器
 *
 * @author Mr.sun
 */
@RestControllerAdvice
public class RRExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(RRException.class)
	public Result<Object> handleRRException(RRException e){
		return Result.error(e.getCode(), e.getMsg());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<Object> handlerNoFoundException(Exception e) {
		logger.error(e.getMessage(), e);
		return Result.error(404, "路径不存在，请检查路径是否正确");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public Result<Object> handleDuplicateKeyException(DuplicateKeyException e){
		logger.error(e.getMessage(), e);
		return Result.error("数据库中已存在该记录");
	}


	@ExceptionHandler(Exception.class)
	public Result<Object> handleException(Exception e){
		logger.error(e.getMessage(), e);
		return Result.error(e.getMessage());
	}
}
