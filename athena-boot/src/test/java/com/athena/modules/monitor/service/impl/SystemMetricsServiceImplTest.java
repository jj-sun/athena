package com.athena.modules.monitor.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemMetricsServiceImplTest {

	private SimpleMeterRegistry meterRegistry;

	private SystemMetricsServiceImpl service;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		service = new SystemMetricsServiceImpl(meterRegistry);
	}

	@Test
	void shouldListRegisteredMetricNames() {
		registerGauge("system.cpu.usage", 0.25);

		assertThat(service.listNames().getNames()).contains("system.cpu.usage");
	}

	@Test
	void shouldReturnMetricDetailFilteredByTag() {
		Counter.builder("demo.requests").tag("status", "success").register(meterRegistry).increment(3);
		Counter.builder("demo.requests").tag("status", "failure").register(meterRegistry).increment(2);

		MetricsEndpoint.MetricDescriptor metric = service.getMetric("demo.requests", List.of("status:success"));

		assertThat(metric).isNotNull();
		assertThat(metric.getMeasurements())
				.anySatisfy(sample -> {
					assertThat(sample.getStatistic()).isEqualTo(Statistic.COUNT);
					assertThat(sample.getValue()).isEqualTo(3);
				});
		assertThat(metric.getAvailableTags()).isEmpty();
		assertThatThrownBy(() -> service.getMetric("demo.requests", List.of("invalid")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("key:value");
	}

	@Test
	void shouldBuildOverviewFromRegisteredMetrics() {
		registerGauge("system.cpu.usage", 0.25);
		registerGauge("process.cpu.usage", 0.125);
		registerGauge("jvm.memory.used", 512, "area", "heap");
		registerGauge("jvm.memory.max", 1024, "area", "heap");
		registerGauge("disk.free", 2048);
		registerGauge("disk.total", 4096);
		registerGauge("jvm.threads.live", 12);
		registerGauge("jvm.threads.peak", 18);
		registerGauge("process.uptime", 3600);
		Counter.builder("http.server.requests").register(meterRegistry).increment(7);

		var overview = service.getOverview();

		assertThat(overview.getTimestamp()).isPositive();
		assertThat(overview.getSystemCpuUsage()).isEqualTo(0.25);
		assertThat(overview.getProcessCpuUsage()).isEqualTo(0.125);
		assertThat(overview.getHeapMemoryUsed()).isEqualTo(512);
		assertThat(overview.getHeapMemoryMax()).isEqualTo(1024);
		assertThat(overview.getDiskFree()).isEqualTo(2048);
		assertThat(overview.getDiskTotal()).isEqualTo(4096);
		assertThat(overview.getLiveThreads()).isEqualTo(12);
		assertThat(overview.getPeakThreads()).isEqualTo(18);
		assertThat(overview.getUptime()).isEqualTo(3600);
		assertThat(overview.getRequestCount()).isEqualTo(7);
	}

	@Test
	void shouldReturnNullForUnavailableOverviewMetrics() {
		var overview = service.getOverview();

		assertThat(overview.getSystemCpuUsage()).isNull();
		assertThat(overview.getHeapMemoryUsed()).isNull();
		assertThat(overview.getDiskTotal()).isNull();
		assertThat(overview.getRequestCount()).isNull();
	}

	private void registerGauge(String name, double value, String... tags) {
		Gauge.builder(name, () -> value).tags(tags).register(meterRegistry);
	}

}
