package com.athena.modules.monitor.service;

import com.alibaba.fastjson.JSONArray;
import com.athena.modules.monitor.domain.RedisInfo;
import com.athena.modules.monitor.exception.RedisConnectException;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.sun
 */
public interface RedisService {

	/**
	 * 获取 redis 的详细信息
	 *
	 * @return List
	 */
	List<RedisInfo> getRedisInfo() throws RedisConnectException;

	/**
	 * 获取 redis key 数量
	 *
	 * @return Map
	 */
	Map<String, Object> getKeysSize() throws RedisConnectException;

	/**
	 * 获取 redis 内存信息
	 *
	 * @return Map
	 */
	Map<String, Object> getMemoryInfo() throws RedisConnectException;
	/**
	 * 获取 报表需要个redis信息
	 *
	 * @return Map
	 */
	Map<String, JSONArray> getMapForReport(String type) throws RedisConnectException ;
}
