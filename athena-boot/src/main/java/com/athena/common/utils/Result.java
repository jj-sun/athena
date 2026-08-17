package com.athena.common.utils;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mr.sun
 */
@Data
public class Result<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private int code;

	private String message;

	private T data;

	private boolean success = true;

	private long timestamp = System.currentTimeMillis();
	
	public Result() {}

	public Result<T> success(String message) {
		this.message = message;
		this.code = HttpStatus.OK.value();
		this.success = true;
		return this;
	}
	
	public static Result<Object> error() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "未知异常，请联系管理员");
	}
	
	public static Result<Object> error(String msg) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
	}
	
	public static Result<Object> error(int code, String msg) {
		Result<Object> r = new Result<>();
		r.setCode(code);
		r.setSuccess(false);
		r.setMessage(msg);
		return r;
	}

	public static<T> Result<T> error(String msg, T data) {
		Result<T> r = new Result<>();
		r.setSuccess(false);
		r.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		r.setMessage(msg);
		r.setData(data);
		return r;
	}

	public static Result<Object> ok() {
		Result<Object> r = new Result<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setMessage("成功");
		return r;
	}

	public static<T> Result<T> ok(T data) {
		Result<T> r = new Result<>();
		r.setSuccess(true);
		r.setData(data);
		r.setCode(HttpStatus.OK.value());
		return r;
	}
	public static<T> Result<T> ok(String msg, T data) {
		Result<T> r = new Result<>();
		r.setSuccess(true);
		r.setCode(HttpStatus.OK.value());
		r.setData(data);
		r.setMessage(msg);
		return r;
	}

}
