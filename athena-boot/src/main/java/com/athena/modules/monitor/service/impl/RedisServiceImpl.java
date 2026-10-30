package com.athena.modules.monitor.service.impl;

import com.athena.modules.monitor.domain.RedisInfo;
import com.athena.modules.monitor.domain.RedisMonitorSnapshot;
import com.athena.modules.monitor.exception.RedisConnectException;
import com.athena.modules.monitor.service.RedisService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Redis 监控信息获取
 *
 * @Author Mr.sun
 */
@Service("redisService")
public class RedisServiceImpl implements RedisService {

	@Resource
	private RedisConnectionFactory redisConnectionFactory;

	@Override
	public RedisMonitorSnapshot getMonitorSnapshot() throws RedisConnectException {
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			Properties info = getInfo(connection);
			RedisMonitorSnapshot snapshot = new RedisMonitorSnapshot();
			snapshot.setTimestamp(System.currentTimeMillis());
			snapshot.setRedisVersion(getString(info, "redis_version"));
			snapshot.setRedisMode(getString(info, "redis_mode"));
			snapshot.setRole(getString(info, "role"));
			snapshot.setUptimeSeconds(getLong(info, "uptime_in_seconds"));
			snapshot.setKeyCount(connection.dbSize());
			snapshot.setUsedMemory(getLong(info, "used_memory"));
			snapshot.setPeakMemory(getLong(info, "used_memory_peak"));
			snapshot.setMaxMemory(getLong(info, "maxmemory"));
			snapshot.setConnectedClients(getLong(info, "connected_clients"));
			snapshot.setBlockedClients(getLong(info, "blocked_clients"));
			snapshot.setOperationsPerSecond(getLong(info, "instantaneous_ops_per_sec"));
			snapshot.setTotalCommandsProcessed(getLong(info, "total_commands_processed"));
			snapshot.setKeyspaceHits(getLong(info, "keyspace_hits"));
			snapshot.setKeyspaceMisses(getLong(info, "keyspace_misses"));
			snapshot.setKeyspaceHitRate(calculateHitRate(snapshot.getKeyspaceHits(), snapshot.getKeyspaceMisses()));
			snapshot.setRejectedConnections(getLong(info, "rejected_connections"));
			snapshot.setDetails(buildRedisInfo(info));
			return snapshot;
		}
		catch (Exception exception) {
			throw new RedisConnectException("Redis 连接失败", exception);
		}
	}

	private Properties getInfo(RedisConnection connection) {
		Properties info = connection.info();
		return info == null ? new Properties() : info;
	}

	private List<RedisInfo> buildRedisInfo(Properties info) {
		return info.entrySet().stream()
				.map(entry -> {
					RedisInfo redisInfo = new RedisInfo();
					redisInfo.setKey(String.valueOf(entry.getKey()));
					redisInfo.setValue(String.valueOf(entry.getValue()));
					return redisInfo;
				})
				.sorted(Comparator.comparing(RedisInfo::getKey))
				.toList();
	}

	private String getString(Properties info, String key) {
		Object value = info.get(key);
		return value == null ? null : value.toString();
	}

	private Long getLong(Properties info, String key) {
		return parseLong(info.get(key));
	}

	private Long parseLong(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(value.toString());
		}
		catch (NumberFormatException ignored) {
			return null;
		}
	}

	private Double calculateHitRate(Long hits, Long misses) {
		if (hits == null || misses == null) {
			return null;
		}
		double total = hits.doubleValue() + misses.doubleValue();
		return total > 0 ? hits / total : null;
	}
}
