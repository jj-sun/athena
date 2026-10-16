package com.athena.modules.monitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.athena.common.utils.Result;
import com.athena.modules.monitor.domain.SystemMetricsOverview;
import com.athena.modules.monitor.service.SystemMetricsService;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys/actuator/metrics")
@SaCheckPermission("sys:monitor:metrics")
public class ActuatorMetricsController {

	private final SystemMetricsService systemMetricsService;

	public ActuatorMetricsController(SystemMetricsService systemMetricsService) {
		this.systemMetricsService = systemMetricsService;
	}

	@GetMapping
	public Result<MetricsEndpoint.MetricNamesDescriptor> listNames() {
		return Result.ok(systemMetricsService.listNames());
	}

	@GetMapping("/overview")
	public Result<SystemMetricsOverview> overview() {
		return Result.ok(systemMetricsService.getOverview());
	}

	@GetMapping("/{metricName}")
	public Result<?> metric(
			@PathVariable String metricName,
			@RequestParam(name = "tag", required = false) List<String> tags) {
		try {
			MetricsEndpoint.MetricDescriptor metric = systemMetricsService.getMetric(
					metricName,
					tags == null ? List.of() : tags);
			if (metric == null) {
				return Result.error(HttpStatus.NOT_FOUND.value(), "指标不存在");
			}
			return Result.ok(metric);
		}
		catch (IllegalArgumentException exception) {
			return Result.error(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
		}
	}

}
