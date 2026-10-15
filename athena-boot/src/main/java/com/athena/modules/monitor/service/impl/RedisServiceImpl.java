package com.athena.modules.monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.athena.modules.monitor.domain.RedisInfo;
import com.athena.modules.monitor.domain.RedisMonitorSnapshot;
import com.athena.modules.monitor.exception.RedisConnectException;
import com.athena.modules.monitor.service.RedisService;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.cglib.beans.BeanMap;
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

	/**
	 * Redis详细信息
	 */
	@Override
	public List<RedisInfo> getRedisInfo() throws RedisConnectException {
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			return buildRedisInfo(getInfo(connection));
		}
		catch (Exception exception) {
			throw new RedisConnectException("Redis 连接失败", exception);
		}
	}

	@Override
	public Map<String, Object> getKeysSize() throws RedisConnectException {
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			Map<String, Object> map = new HashMap<>();
			map.put("create_time", System.currentTimeMillis());
			map.put("dbSize", connection.dbSize());
			return map;
		}
		catch (Exception exception) {
			throw new RedisConnectException("Redis 连接失败", exception);
		}
	}

	@Override
	public Map<String, Object> getMemoryInfo() throws RedisConnectException {
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			Properties info = getInfo(connection);
			Map<String, Object> map = new HashMap<>();
			map.put("used_memory", getString(info, "used_memory"));
			map.put("create_time", System.currentTimeMillis());
			return map;
		}
		catch (Exception exception) {
			throw new RedisConnectException("Redis 连接失败", exception);
		}
	}

    /**
     * 查询redis信息for报表
     * @param type 1redis key数量 2 占用内存 3redis信息
     * @return
     * @throws RedisConnectException
     */
	@Override
	public Map<String, JSONArray> getMapForReport(String type)  throws RedisConnectException {
		Map<String,JSONArray> mapJson=new HashMap<String, JSONArray> ();
		JSONArray json = new JSONArray();
		if("3".equals(type)){
			List<RedisInfo> redisInfo = getRedisInfo();
			for(RedisInfo info:redisInfo){
				Map<String, Object> map= Maps.newHashMap();
				BeanMap beanMap = BeanMap.create(info);
				for (Object key : beanMap.keySet()) {
					map.put(key+"", beanMap.get(key));
				}
				json.add(map);
			}
			mapJson.put("data",json);
			return mapJson;
		}
		for(int i = 0; i < 5; i++){
			JSONObject jo = new JSONObject();
			Map<String, Object> map;
			if("1".equals(type)){
				map= getKeysSize();
				jo.put("value",map.get("dbSize"));
			}else{
				map = getMemoryInfo();
				Long usedMemory = parseLong(map.get("used_memory"));
				jo.put("value", usedMemory == null ? null : usedMemory / 1000);
			}
			String create_time = DateUtil.formatTime(DateUtil.date((Long) map.get("create_time")-(4-i)*1000));
			jo.put("name",create_time);
			json.add(jo);
		}
		mapJson.put("data",json);
		return mapJson;
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
