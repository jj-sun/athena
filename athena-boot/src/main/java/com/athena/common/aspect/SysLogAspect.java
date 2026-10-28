package com.athena.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.athena.common.annotation.Log;
import com.athena.common.constant.Constant;
import com.athena.common.utils.ClientIpResolver;
import com.athena.common.utils.HttpContextUtils;
import com.athena.modules.sys.entity.SysLog;
import com.athena.modules.sys.service.SysLogService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


/**
 * 系统日志，切面处理类
 *
 * @author Mr.sun
 */
@Aspect
@Component
public class SysLogAspect {

	/** 敏感字段的脱敏占位符 */
	private static final String MASK = "***";

	/** 参数日志最大长度，避免超大请求体撑爆审计表 */
	private static final int MAX_PARAMS_LENGTH = 4000;

	@Autowired
	private SysLogService sysLogService;
	@Autowired
	private ClientIpResolver clientIpResolver;
	
	@Pointcut("@annotation(com.athena.common.annotation.Log)")
	public void logPointCut() { 
		
	}

	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		long beginTime = System.currentTimeMillis();
		//执行方法
		Object result = point.proceed();
		//执行时长(毫秒)
		long time = System.currentTimeMillis() - beginTime;

		//保存日志
		saveSysLog(point, time);

		return result;
	}

	private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		SysLog sysLog = new SysLog();
		sysLog.setLogType(Constant.LogType.OPERATE.getValue());
		Log syslog = method.getAnnotation(Log.class);
		if(syslog != null){
			//注解上的描述
			sysLog.setOperation(syslog.value());
		}

		//请求的方法名
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = signature.getName();
		sysLog.setMethod(className + "." + methodName + "()");

		//请求的参数（脱敏后保存）
		sysLog.setParams(toSafeJson(joinPoint.getArgs()));

		//获取request
		HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
		//设置IP地址（仅当请求来自可信代理时才采信 X-Forwarded-For）
		sysLog.setIp(clientIpResolver.resolve(request));
		sysLog.setTime(time);

		//用户名
		String username = StpUtil.getLoginIdAsString();
		sysLog.setUsername(username);

		sysLog.setTime(time);
		sysLog.setCreateDate(new Date());
		//保存系统日志
		sysLogService.save(sysLog);
	}

	/**
	 * 序列化方法入参并做敏感字段脱敏。
	 *
	 * <p>不能直接 {@code Gson.toJson(args)}：Gson 不识别 Jackson 的
	 * {@code @JsonProperty(WRITE_ONLY)}，会把密码哈希、RSA 密文等凭证材料写入
	 * {@code sys_log.params}，而该字段可通过 {@code /sys/log/list} 查询并在前端展示。
	 */
	private String toSafeJson(Object[] args) {
		try {
			JsonElement element = JsonParser.parseString(new Gson().toJson(args));
			redact(element);
			String json = element.toString();
			return json.length() > MAX_PARAMS_LENGTH ? json.substring(0, MAX_PARAMS_LENGTH) : json;
		} catch (Exception e) {
			// 序列化失败时不要把原始参数写进日志
			return "[]";
		}
	}

	private static void redact(JsonElement element) {
		if (element == null) {
			return;
		}
		if (element.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				if (isSensitive(entry.getKey())) {
					entry.setValue(new JsonPrimitive(MASK));
				} else {
					redact(entry.getValue());
				}
			}
		} else if (element.isJsonArray()) {
			for (JsonElement child : element.getAsJsonArray()) {
				redact(child);
			}
		}
	}

	private static boolean isSensitive(String key) {
		if (key == null) {
			return false;
		}
		String k = key.toLowerCase(Locale.ROOT);
		return k.contains("password") || k.contains("passwd")
				|| k.contains("token") || k.contains("secret")
				|| k.contains("salt") || k.contains("privatekey")
				|| "sign".equals(k);
	}
}
