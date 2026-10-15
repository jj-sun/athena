package com.athena.modules.monitor.service.impl;

import com.athena.modules.monitor.domain.SystemMetricsOverview;
import com.athena.modules.monitor.service.SystemMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemMetricsServiceImpl implements SystemMetricsService {

	private static final List<String> HEAP_TAG = List.of("area:heap");

	private final MetricsEndpoint metricsEndpoint;

	public SystemMetricsServiceImpl(MeterRegistry meterRegistry) {
		this.metricsEndpoint = new MetricsEndpoint(meterRegistry);
	}

	@Override
	public SystemMetricsOverview getOverview() {
		SystemMetricsOverview overview = new SystemMetricsOverview();
		overview.setTimestamp(System.currentTimeMillis());
		overview.setSystemCpuUsage(getValue("system.cpu.usage", Statistic.VALUE, List.of()));
		overview.setProcessCpuUsage(getValue("process.cpu.usage", Statistic.VALUE, List.of()));
		overview.setHeapMemoryUsed(getValue("jvm.memory.used", Statistic.VALUE, HEAP_TAG));
		overview.setHeapMemoryMax(getValue("jvm.memory.max", Statistic.VALUE, HEAP_TAG));
		overview.setDiskFree(getValue("disk.free", Statistic.VALUE, List.of()));
		overview.setDiskTotal(getValue("disk.total", Statistic.VALUE, List.of()));
		overview.setLiveThreads(getValue("jvm.threads.live", Statistic.VALUE, List.of()));
		overview.setPeakThreads(getValue("jvm.threads.peak", Statistic.VALUE, List.of()));
		overview.setUptime(getValue("process.uptime", Statistic.VALUE, List.of()));
		overview.setRequestCount(getValue("http.server.requests", Statistic.COUNT, List.of()));
		return overview;
	}

	@Override
	public MetricsEndpoint.MetricNamesDescriptor listNames() {
		return metricsEndpoint.listNames();
	}

	@Override
	public MetricsEndpoint.MetricDescriptor getMetric(String name, List<String> tags) {
		validateTags(tags);
		return metricsEndpoint.metric(name, tags);
	}

	private Double getValue(String name, Statistic statistic, List<String> tags) {
		MetricsEndpoint.MetricDescriptor metric = metricsEndpoint.metric(name, tags);
		if (metric == null) {
			return null;
		}
		return metric.getMeasurements()
				.stream()
				.filter(sample -> statistic.equals(sample.getStatistic()))
				.map(MetricsEndpoint.Sample::getValue)
				.filter(value -> value != null && Double.isFinite(value))
				.findFirst()
				.orElse(null);
	}

	private void validateTags(List<String> tags) {
		for (String tag : tags) {
			int separatorIndex = tag.indexOf(':');
			if (separatorIndex <= 0 || separatorIndex == tag.length() - 1) {
				throw new IllegalArgumentException("指标标签必须使用 key:value 格式");
			}
		}
	}

}
