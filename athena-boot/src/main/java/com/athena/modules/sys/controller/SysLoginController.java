package com.athena.modules.sys.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.athena.common.annotation.Log;
import com.athena.common.constant.SecurityConstant;
import com.athena.common.utils.RedisUtils;
import com.athena.common.utils.Result;
import com.athena.modules.sys.entity.SysUser;
import com.athena.modules.sys.form.LoginUser;
import com.athena.modules.sys.form.SysLoginForm;
import com.athena.modules.sys.service.SysLogService;
import com.athena.modules.sys.service.SysUserService;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * 登录相关
 *
 * @author Mr.sun
 */
@RestController
@Slf4j
public class SysLoginController {


	@Autowired
	private SysUserService userService;

	@Autowired
	private SysLogService logService;

	@Resource
	private RedisUtils redisUtils;

	@PostMapping(value = "/login")
	public Result<?> login(@RequestBody SysLoginForm loginForm) {
		//首先校验验证码
		/*String captcha = loginForm.getCaptcha();
		if(captcha==null){
			return Result.error(500, "验证码无效");
		}
		String lowerCaseCaptcha = captcha.toLowerCase();
		String key = SecureUtil.md5(loginForm.getCheckKey() + ":" + lowerCaseCaptcha);
		Object checkCode = redisUtils.get(key);
		//当进入登录页时，有一定几率出现验证码错误 #1714
		if(checkCode==null || !checkCode.toString().equals(lowerCaseCaptcha)) {
			return Result.error(500, "验证码无效");
		}*/

		boolean loginStatus = false;
		String username = loginForm.getUsername();
		if(StringUtils.isBlank(username)) {
			return Result.error("用户名必填！");
		}
		SysUser user = userService.queryByUserName(username);
		if(Objects.isNull(user)) {
			return Result.error("信息有误！");
		}
		String md5 = SaSecureUtil.md5(loginForm.getUsername() + ":" + loginForm.getPassword());
		loginStatus = BCrypt.checkpw(md5, user.getPassword());
		//redisUtils.delete(key);
		//TODO 登录日志
		if(loginStatus) {
			Map<String, Object> userMap = userInfo(user);
			StpUtil.login(username, SaLoginConfig.setExtraData(userMap));
			SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
			Map<String, Object> result = Maps.newHashMap();
			result.put("token", saTokenInfo.getTokenValue());
			result.put("userInfo", userMap);
			return Result.ok("登录成功", result);
		}
		return Result.error("用户名或密码错误！");
	}

	public Map<String,Object> userInfo(SysUser user) {
		LoginUser loginUser = new LoginUser();
		BeanUtils.copyProperties(user, loginUser);
		return BeanUtil.beanToMap(loginUser);
	}


	@Log("登出")
	@GetMapping(value = "/logout")
	public Result<?> logout(HttpServletRequest request) {
		//用户退出逻辑
		String token = request.getHeader(SecurityConstant.X_ACCESS_TOKEN);
		if(StringUtils.isEmpty(token)) {
			return Result.error("退出登录失败！");
		}
		String username = StpUtil.getExtra(token, "username").toString();
		SysUser sysUser = userService.queryByUserName(username);
		if(sysUser!=null) {
			//update-begin--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
			//logService.addLog("用户名: "+sysUser.getRealname()+",退出成功！", CommonConstant.LOG_TYPE_1, null,sysUser);
			//update-end--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
			log.info(" 用户名:  "+sysUser.getRealname()+",退出成功！ ");
			StpUtil.logoutByTokenValue(token);
			return Result.ok("退出登录成功！");
		}else {
			return Result.error("Token无效!");
		}
	}


	/**
	 * 验证码
	 */
	@GetMapping("/captcha")
	public Result<?> captcha(HttpServletResponse response, String checkKey)throws IOException {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
		String verCode = captcha.getCode();

		String key = SecureUtil.md5(checkKey + ":" + verCode.toLowerCase());
		// 30分钟
		redisUtils.set(key, verCode, 30 * 60);
		return Result.ok(captcha.getImageBase64());
	}
}
