package com.athena.modules.monitor.domain;

import lombok.Data;

import java.util.List;

/**
 * Redis monitoring snapshot.
 */
@Data
public class RedisMonitorSnapshot {

	private long timestamp;

	private String redisVersion;

	private String redisMode;

	private String role;

	private Long uptimeSeconds;

	private Long keyCount;

	private Long usedMemory;

	private Long peakMemory;

	private Long maxMemory;

	private Long connectedClients;

	private Long blockedClients;

	private Long operationsPerSecond;

	private Long totalCommandsProcessed;

	private Long keyspaceHits;

	private Long keyspaceMisses;

	private Double keyspaceHitRate;

	private Long rejectedConnections;

	private List<RedisInfo> details;

}
