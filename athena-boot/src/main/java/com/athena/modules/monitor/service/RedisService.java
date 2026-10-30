package com.athena.modules.monitor.service;

import com.athena.modules.monitor.domain.RedisMonitorSnapshot;
import com.athena.modules.monitor.exception.RedisConnectException;

/**
 * @author Mr.sun
 */
public interface RedisService {

	/**
	 * 获取 Redis 监控快照
	 *
	 * @return Redis 监控快照
	 */
	RedisMonitorSnapshot getMonitorSnapshot() throws RedisConnectException;
}
