package com.athena.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.athena.common.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	/**
	 * 未登录：返回 401，前端据此跳转登录页。
	 */
	@ExceptionHandler(NotLoginException.class)
	public ResponseEntity<Result<Object>> handleNotLoginException(NotLoginException e){
		logger.warn("未登录访问被拒绝: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Result.error(HttpStatus.UNAUTHORIZED.value(), "登录状态无效，请重新登录"));
	}

	/**
	 * 权限不足：返回 403。注意不能与 401 混用，否则权限不足会把用户踢下线。
	 */
	@ExceptionHandler(NotPermissionException.class)
	public ResponseEntity<Result<Object>> handleNotPermissionException(NotPermissionException e){
		logger.warn("权限不足被拒绝: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Result.error(HttpStatus.FORBIDDEN.value(), "无操作权限"));
	}

	@ExceptionHandler(NotRoleException.class)
	public ResponseEntity<Result<Object>> handleNotRoleException(NotRoleException e){
		logger.warn("角色不足被拒绝: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Result.error(HttpStatus.FORBIDDEN.value(), "无操作权限"));
	}

	/**
	 * 兜底异常。
	 * 注意：不能把 e.getMessage() 直接返回给客户端 —— 那会把内部异常原文（含 SQL 语句、
	 * 表名列名、堆栈摘要等）泄露出去。对外统一返回通用文案，细节只记服务端日志。
	 */
	@ExceptionHandler(Exception.class)
	public Result<Object> handleException(Exception e){
		logger.error(e.getMessage(), e);
		return Result.error("系统繁忙，请稍后重试");
	}
}
