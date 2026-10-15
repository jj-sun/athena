package com.athena.modules.monitor.service;

import com.athena.modules.monitor.domain.SystemMetricsOverview;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;

import java.util.List;

public interface SystemMetricsService {

	SystemMetricsOverview getOverview();

	MetricsEndpoint.MetricNamesDescriptor listNames();

	MetricsEndpoint.MetricDescriptor getMetric(String name, List<String> tags);

}
