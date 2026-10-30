package com.athena.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.utils.Result;
import com.athena.modules.monitor.domain.RedisMonitorSnapshot;
import com.athena.modules.monitor.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.sun
 */
@RestController
@RequestMapping("/sys/actuator/redis")
@SaCheckPermission("sys:monitor:redis")
public class ActuatorRedisController {

    @Autowired
    private RedisService redisService;

    @GetMapping("/snapshot")
    public Result<RedisMonitorSnapshot> getMonitorSnapshot() throws Exception {
        return Result.ok(redisService.getMonitorSnapshot());
    }
}
