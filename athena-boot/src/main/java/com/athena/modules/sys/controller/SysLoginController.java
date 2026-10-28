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
import com.athena.common.utils.ClientIpResolver;
import com.athena.common.utils.RateLimitService;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 登录相关
 *
 * @author Mr.sun
 */
@RestController
@Slf4j
public class SysLoginController {

	/**
	 * 登录失败的统一提示。
	 * "用户不存在"与"密码错误"必须返回同一文案，否则可被用来枚举系统账号。
	 */
	private static final String LOGIN_FAIL_MESSAGE = "用户名或密码错误！";

	/** 验证码有效期（秒） */
	private static final long CAPTCHA_EXPIRE_SECONDS = 5 * 60L;

	/** checkKey 只允许字母、数字、下划线与连字符，最长 64，避免被用来膨胀 Redis 键 */
	private static final Pattern CHECK_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

	/**
	 * 用于抹平响应时间差的固定 BCrypt 哈希。
	 * 用户不存在时对固定值执行一次 checkpw，使耗时与"用户存在但密码错误"一致。
	 */
	private static final String TIMING_DUMMY_HASH =
			BCrypt.hashpw("athena-timing-equalization-dummy", BCrypt.gensalt());

	@Autowired
	private SysUserService userService;

	@Autowired
	private SysLogService logService;

	@Resource
	private RedisUtils redisUtils;

	@Autowired
	private ClientIpResolver clientIpResolver;

	@Autowired
	private RateLimitService rateLimitService;

	@Value("${athena.security.captcha-rate-limit:10}")
	private int captchaRateLimit;

	@Value("${athena.security.captcha-rate-window-seconds:60}")
	private long captchaRateWindowSeconds;

	@Value("${athena.rsa.private-key}")
	private String rsaPrivateKey;

	@PostMapping(value = "/login")
	public Result<?> login(@RequestBody SysLoginForm loginForm) {
		// 先校验验证码：失败路径不涉及解密与数据库查询，成本更低，也避免被用来探测账号
		String captchaError = verifyCaptcha(loginForm.getCheckKey(), loginForm.getCaptcha());
		if (captchaError != null) {
			return Result.error(captchaError);
		}

		String username = loginForm.getUsername();
		String password = loginForm.getPassword();
		try {
			// 登录信息前端 RSA 公钥加密，此处用私钥解密
			username = SaSecureUtil.rsaDecryptByPrivate(rsaPrivateKey, username);
			password = SaSecureUtil.rsaDecryptByPrivate(rsaPrivateKey, password);
		} catch (Exception e) {
			return Result.error(LOGIN_FAIL_MESSAGE);
		}
		if(StringUtils.isBlank(username)) {
			return Result.error("用户名必填！");
		}

		SysUser user = userService.queryByUserName(username);
		if(Objects.isNull(user)) {
			// 用户不存在时同样跑一次 BCrypt 校验（对固定哈希），让响应耗时与真实校验接近。
			// 仅统一错误文案是不够的：BCrypt 明显慢于直接返回，耗时会泄露账号是否存在。
			BCrypt.checkpw(username, TIMING_DUMMY_HASH);
			return Result.error(LOGIN_FAIL_MESSAGE);
		}

		String md5 = SaSecureUtil.md5(username + ":" + password);
		if(BCrypt.checkpw(md5, user.getPassword())) {
			Map<String, Object> userMap = userInfo(user);
			StpUtil.login(username, SaLoginConfig.setExtraData(userMap));
			SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
			Map<String, Object> result = Maps.newHashMap();
			result.put("token", saTokenInfo.getTokenValue());
			result.put("userInfo", userMap);
			return Result.ok("登录成功", result);
		}
		return Result.error(LOGIN_FAIL_MESSAGE);
	}

	/**
	 * 校验验证码，无论成功与否都会消费掉 Redis 中的验证码（一次性）。
	 *
	 * @return 校验通过返回 null；否则返回给前端的错误提示
	 */
	private String verifyCaptcha(String checkKey, String captcha) {
		if (StringUtils.isBlank(checkKey)) {
			return "验证码已失效，请刷新后重试";
		}
		if (StringUtils.isBlank(captcha)) {
			return "请输入验证码";
		}
		// 生成时以 md5(checkKey:code) 作为 key、以小写 code 作为值写入，此处用用户输入重算 key
		String input = captcha.trim().toLowerCase();
		String key = SecureUtil.md5(checkKey + ":" + input);
		String stored = redisUtils.get(key);
		// 先消费再判定：防止同一验证码被反复尝试
		redisUtils.delete(key);
		if (stored == null || !stored.equals(input)) {
			return "验证码错误";
		}
		return null;
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
		Object usernameExtra = StpUtil.getExtra(token, "username");
		if (usernameExtra == null) {
			return Result.error("Token无效!");
		}
		String username = usernameExtra.toString();
		SysUser sysUser = userService.queryByUserName(username);
		if(sysUser!=null) {
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
	public Result<?> captcha(String checkKey, HttpServletRequest request) {
		if (StringUtils.isBlank(checkKey) || !CHECK_KEY_PATTERN.matcher(checkKey).matches()) {
			// checkKey 为空会退化成 md5("null:code")，超长/非法值则会被用来膨胀 Redis 键空间
			return Result.error("无效的 checkKey");
		}

		// 按客户端 IP 限流，防止匿名用户无限生成验证码（CPU 与 Redis 内存消耗）
		String clientIp = clientIpResolver.resolve(request);
		if (!rateLimitService.tryAcquire("sys:rate:captcha:" + clientIp,
				captchaRateLimit, captchaRateWindowSeconds)) {
			return Result.error(429, "请求过于频繁，请稍后再试");
		}

		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);
		// 统一按小写存储：Hutool 生成的验证码含大小写字母，而校验时按小写比对，
		// 若此处存原始大小写，凡是含大写字母的验证码都会校验失败（表现为偶发登录失败）。
		String verCode = captcha.getCode().toLowerCase();

		String key = SecureUtil.md5(checkKey + ":" + verCode);
		redisUtils.set(key, verCode, CAPTCHA_EXPIRE_SECONDS);
		return Result.ok(captcha.getImageBase64());
	}
}
