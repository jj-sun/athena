package com.athena.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 系统日志
 *
 * @author Mr.sun
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	@TableId
	private String id;
	//日志类型0登录日志1操作日志
	private Integer logType;
	//用户名
	private String username;
	//用户操作
	private String operation;
	//请求方法
	private String method;
	//请求参数
	private String params;
	//执行时长(毫秒)
	private Long time;
	//IP地址
	private String ip;
	//创建时间
	private Date createDate;

}
