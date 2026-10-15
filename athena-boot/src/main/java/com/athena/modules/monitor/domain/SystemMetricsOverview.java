package com.athena.modules.monitor.domain;

import lombok.Data;

/**
 * Core system metrics used by the monitoring overview.
 */
@Data
public class SystemMetricsOverview {

	private long timestamp;

	private Double systemCpuUsage;

	private Double processCpuUsage;

	private Double heapMemoryUsed;

	private Double heapMemoryMax;

	private Double diskFree;

	private Double diskTotal;

	private Double liveThreads;

	private Double peakThreads;

	private Double uptime;

	private Double requestCount;

}
