import type { MetricDetail, MetricNamesResponse, SystemMetricsOverview } from '@/types/monitor';
import { GET } from '@/utils/http';

const metricsApi = {
  root: '/sys/actuator/metrics',
  overview: '/sys/actuator/metrics/overview',
};

export function getSystemMetricsOverview() {
  return GET<SystemMetricsOverview>(metricsApi.overview);
}

export function getMetricNames() {
  return GET<MetricNamesResponse>(metricsApi.root);
}

export function getMetricDetail(name: string, tags: Record<string, string> = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(tags).forEach(([key, value]) => {
    if (value) {
      searchParams.append('tag', `${key}:${value}`);
    }
  });
  const query = searchParams.toString();
  const url = `${metricsApi.root}/${encodeURIComponent(name)}${query ? `?${query}` : ''}`;
  return GET<MetricDetail>(url);
}
