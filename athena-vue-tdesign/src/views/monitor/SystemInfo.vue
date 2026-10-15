<template>
  <div class="main-container system-info">
    <div class="monitor-toolbar">
      <div class="monitor-status">
        <t-tag :theme="statusTheme">
          {{ statusText }}
        </t-tag>
        <span class="monitor-status__time">最近更新：{{ lastUpdatedText }}</span>
      </div>
      <t-space align="center">
        <span class="refresh-label">刷新频率</span>
        <t-select v-model:value="refreshInterval" :options="refreshOptions" class="refresh-select" />
        <t-tooltip :content="isPaused ? '继续自动刷新' : '暂停自动刷新'">
          <t-button
            shape="square"
            variant="outline"
            :aria-label="isPaused ? '继续自动刷新' : '暂停自动刷新'"
            @click="togglePolling"
          >
            <template #icon>
              <play-icon v-if="isPaused" />
              <pause-icon v-else />
            </template>
          </t-button>
        </t-tooltip>
        <t-tooltip content="立即刷新">
          <t-button
            theme="primary"
            shape="square"
            :loading="activeRefreshing"
            aria-label="立即刷新"
            @click="refreshActiveData"
          >
            <template #icon><refresh-icon /></template>
          </t-button>
        </t-tooltip>
      </t-space>
    </div>

    <t-tabs v-model:value="activeTab" class="monitor-tabs">
      <t-tab-panel value="system" label="系统指标">
        <div class="monitor-tab-content">
          <t-row :gutter="[16, 16]" class="overview-grid">
            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--brand"><cpu-icon /></span>
                  <span>系统 CPU</span>
                </div>
                <template v-if="systemCpuPercent !== null">
                  <t-statistic :value="systemCpuPercent" :decimal-places="1" unit="%" />
                  <t-progress
                    class="metric-card__progress"
                    :percentage="systemCpuPercent"
                    :label="false"
                    :color="progressColor(systemCpuPercent)"
                  />
                </template>
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">整机最近使用率</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--success"><activity-icon /></span>
                  <span>进程 CPU</span>
                </div>
                <template v-if="processCpuPercent !== null">
                  <t-statistic :value="processCpuPercent" :decimal-places="1" unit="%" />
                  <t-progress
                    class="metric-card__progress"
                    :percentage="processCpuPercent"
                    :label="false"
                    :color="progressColor(processCpuPercent)"
                  />
                </template>
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">JVM 进程最近使用率</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--warning"><server-icon /></span>
                  <span>JVM 堆内存</span>
                </div>
                <div v-if="overview?.heapMemoryUsed !== null" class="metric-card__value">
                  {{ formatBytes(overview?.heapMemoryUsed) }}
                </div>
                <div v-else class="metric-card__empty">--</div>
                <t-progress
                  v-if="heapUsagePercent !== null"
                  class="metric-card__progress"
                  :percentage="heapUsagePercent"
                  :label="false"
                  :color="progressColor(heapUsagePercent)"
                />
                <div class="metric-card__meta">上限 {{ formatBytes(overview?.heapMemoryMax) }}</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--danger"><hard-disk-storage-icon /></span>
                  <span>磁盘使用</span>
                </div>
                <div v-if="diskUsed !== null" class="metric-card__value">{{ formatBytes(diskUsed) }}</div>
                <div v-else class="metric-card__empty">--</div>
                <t-progress
                  v-if="diskUsagePercent !== null"
                  class="metric-card__progress"
                  :percentage="diskUsagePercent"
                  :label="false"
                  :color="progressColor(diskUsagePercent)"
                />
                <div class="metric-card__meta">总计 {{ formatBytes(overview?.diskTotal) }}</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--success"><task-time-icon /></span>
                  <span>活动线程</span>
                </div>
                <t-statistic
                  v-if="overview?.liveThreads !== null && overview?.liveThreads !== undefined"
                  :value="overview.liveThreads"
                  :decimal-places="0"
                  unit=" 个"
                />
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">峰值 {{ formatCount(overview?.peakThreads) }}</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="initialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--brand"><time-icon /></span>
                  <span>运行时长</span>
                </div>
                <div class="metric-card__value metric-card__value--duration">
                  {{ formatDuration(overview?.uptime) }}
                </div>
                <div class="metric-card__meta">累计请求 {{ formatCount(overview?.requestCount) }}</div>
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="trend-grid">
            <t-col :xs="12" :xl="6">
              <t-card title="CPU 使用趋势" class="panel-card" :bordered="false" :loading="initialLoading">
                <div ref="cpuChartContainer" class="trend-chart" />
              </t-card>
            </t-col>
            <t-col :xs="12" :xl="6">
              <t-card title="JVM 堆内存趋势" class="panel-card" :bordered="false" :loading="initialLoading">
                <div ref="memoryChartContainer" class="trend-chart" />
              </t-card>
            </t-col>
          </t-row>

          <t-card title="度量指标" class="panel-card metric-browser" :bordered="false">
            <template #actions>
              <t-select
                v-model:value="selectedMetric"
                class="metric-selector"
                :options="metricOptions"
                :loading="initialLoading"
                filterable
                placeholder="请选择指标"
                @change="handleMetricChange"
              />
            </template>

            <t-loading :loading="metricLoading" size="small">
              <template v-if="metricDetail">
                <t-descriptions :column="descriptionColumns" bordered size="small">
                  <t-descriptions-item label="指标名称">{{ metricDetail.name }}</t-descriptions-item>
                  <t-descriptions-item label="基础单位">{{ metricDetail.baseUnit || '无' }}</t-descriptions-item>
                  <t-descriptions-item label="指标说明">{{
                    metricDetail.description || '暂无说明'
                  }}</t-descriptions-item>
                </t-descriptions>

                <div v-if="metricTagOptions.length" class="tag-filter-list">
                  <div v-for="item in metricTagOptions" :key="item.tag" class="tag-filter">
                    <span class="tag-filter__label">{{ item.tag }}</span>
                    <t-select
                      :value="selectedTags[item.tag]"
                      :options="item.values.map((value) => ({ label: value, value }))"
                      clearable
                      filterable
                      placeholder="全部"
                      @change="(value) => handleTagChange(item.tag, value)"
                    />
                  </div>
                </div>

                <t-table
                  class="measurement-table"
                  row-key="statistic"
                  :data="measurementRows"
                  :columns="measurementColumns"
                  table-layout="fixed"
                  size="small"
                >
                  <template #value="{ row }">
                    <span class="measurement-value">{{ row.displayValue }}</span>
                  </template>
                </t-table>
              </template>
              <t-empty v-else description="暂无指标详情" />
            </t-loading>
          </t-card>
        </div>
      </t-tab-panel>

      <t-tab-panel value="redis" label="Redis">
        <div class="monitor-tab-content">
          <t-alert
            v-if="!redisConnectionHealthy && !redisSnapshot"
            theme="error"
            message="Redis 连接异常，正在等待下次刷新"
          />

          <t-row :gutter="[16, 16]" class="overview-grid">
            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--brand"><server-icon /></span>
                  <span>Redis 实例</span>
                </div>
                <div v-if="redisSnapshot?.redisVersion" class="metric-card__value metric-card__value--text">
                  v{{ redisSnapshot.redisVersion }}
                </div>
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">
                  {{ redisIdentityText }} · {{ formatDuration(redisSnapshot?.uptimeSeconds) }}
                </div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--success"><key-icon /></span>
                  <span>当前库 Key 数</span>
                </div>
                <t-statistic
                  v-if="redisSnapshot?.keyCount !== null && redisSnapshot?.keyCount !== undefined"
                  :value="redisSnapshot.keyCount"
                  :decimal-places="0"
                  unit=" 个"
                />
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">当前 Redis 数据库</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--warning"><hard-disk-storage-icon /></span>
                  <span>Redis 内存</span>
                </div>
                <div v-if="redisSnapshot?.usedMemory !== null" class="metric-card__value">
                  {{ formatBytes(redisSnapshot?.usedMemory) }}
                </div>
                <div v-else class="metric-card__empty">--</div>
                <t-progress
                  v-if="redisMemoryUsagePercent !== null"
                  class="metric-card__progress"
                  :percentage="redisMemoryUsagePercent"
                  :label="false"
                  :color="progressColor(redisMemoryUsagePercent)"
                />
                <div class="metric-card__meta">{{ redisMemoryMeta }}</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--success"><activity-icon /></span>
                  <span>客户端连接</span>
                </div>
                <t-statistic
                  v-if="redisSnapshot?.connectedClients !== null && redisSnapshot?.connectedClients !== undefined"
                  :value="redisSnapshot.connectedClients"
                  :decimal-places="0"
                  unit=" 个"
                />
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">阻塞 {{ formatCount(redisSnapshot?.blockedClients) }} 个</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--brand"><chart-icon /></span>
                  <span>实时吞吐</span>
                </div>
                <t-statistic
                  v-if="redisSnapshot?.operationsPerSecond !== null && redisSnapshot?.operationsPerSecond !== undefined"
                  :value="redisSnapshot.operationsPerSecond"
                  :decimal-places="0"
                  unit=" ops/s"
                />
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">累计命令 {{ formatCount(redisSnapshot?.totalCommandsProcessed) }}</div>
              </t-card>
            </t-col>

            <t-col :xs="12" :md="6" :xl="4" :xxl="2">
              <t-card class="metric-card" :loading="redisInitialLoading">
                <div class="metric-card__header">
                  <span class="metric-card__icon metric-card__icon--danger"><task-time-icon /></span>
                  <span>Key 命中率</span>
                </div>
                <t-statistic
                  v-if="redisHitRatePercent !== null"
                  :value="redisHitRatePercent"
                  :decimal-places="1"
                  unit="%"
                />
                <div v-else class="metric-card__empty">--</div>
                <div class="metric-card__meta">拒绝连接 {{ formatCount(redisSnapshot?.rejectedConnections) }}</div>
              </t-card>
            </t-col>
          </t-row>

          <t-row :gutter="[16, 16]" class="trend-grid">
            <t-col :xs="12" :xl="6">
              <t-card title="Redis 内存趋势" class="panel-card" :bordered="false" :loading="redisInitialLoading">
                <div ref="redisMemoryChartContainer" class="trend-chart" />
              </t-card>
            </t-col>
            <t-col :xs="12" :xl="6">
              <t-card title="Redis 吞吐趋势" class="panel-card" :bordered="false" :loading="redisInitialLoading">
                <div ref="redisOpsChartContainer" class="trend-chart" />
              </t-card>
            </t-col>
          </t-row>

          <t-card title="Redis INFO" class="panel-card redis-info" :bordered="false">
            <template #actions>
              <t-input v-model:value="redisSearch" class="redis-search" placeholder="搜索参数、值或说明" clearable>
                <template #prefix-icon><search-icon /></template>
              </t-input>
            </template>

            <t-loading :loading="redisInitialLoading" size="small">
              <t-table
                v-if="filteredRedisDetails.length"
                row-key="key"
                :data="filteredRedisDetails"
                :columns="redisInfoColumns"
                table-layout="fixed"
                size="small"
              >
                <template #value="{ row }">
                  <t-tooltip :content="row.value">
                    <span class="redis-info__value">{{ row.value }}</span>
                  </t-tooltip>
                </template>
                <template #description="{ row }">
                  <t-tooltip :content="row.description || '暂无说明'">
                    <span class="redis-info__description">{{ row.description || '暂无说明' }}</span>
                  </t-tooltip>
                </template>
              </t-table>
              <t-empty v-else description="暂无 Redis 信息" />
            </t-loading>
          </t-card>
        </div>
      </t-tab-panel>
    </t-tabs>
  </div>
</template>
<script lang="ts" setup>
import { useWindowSize } from '@vueuse/core';
import dayjs from 'dayjs';
import { LineChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import * as echarts from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import {
  ActivityIcon,
  ChartIcon,
  CpuIcon,
  HardDiskStorageIcon,
  KeyIcon,
  PauseIcon,
  PlayIcon,
  RefreshIcon,
  SearchIcon,
  ServerIcon,
  TaskTimeIcon,
  TimeIcon,
} from 'tdesign-icons-vue-next';
import type { PrimaryTableCol, SelectValue, TagProps } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onActivated, onDeactivated, onMounted, onUnmounted, reactive, ref, watch } from 'vue';

import { getMetricDetail, getMetricNames, getSystemMetricsOverview } from '@/api/monitor/metrics';
import { getRedisMonitorSnapshot } from '@/api/monitor/redis';
import { useSettingStore } from '@/store';
import type { MetricAvailableTag, MetricDetail, RedisMonitorSnapshot, SystemMetricsOverview } from '@/types/monitor';

interface TrendPoint {
  time: string;
  systemCpu: number | null;
  processCpu: number | null;
  heapUsage: number | null;
}

interface RedisTrendPoint {
  time: string;
  usedMemory: number | null;
  operationsPerSecond: number | null;
}

interface MeasurementRow {
  statistic: string;
  value: number;
  displayValue: string;
  unit: string;
}

defineOptions({
  name: 'SystemInfo',
});
type MonitorTab = 'system' | 'redis';
const DEFAULT_METRIC = 'jvm.memory.used';
const MAX_TREND_POINTS = 60;

echarts.use([GridComponent, LegendComponent, TooltipComponent, LineChart, CanvasRenderer]);

const settingStore = useSettingStore();
const { width: viewportWidth } = useWindowSize();
const activeTab = ref<MonitorTab>('system');
const cpuChartContainer = ref<HTMLElement | null>(null);
const memoryChartContainer = ref<HTMLElement | null>(null);
const redisMemoryChartContainer = ref<HTMLElement | null>(null);
const redisOpsChartContainer = ref<HTMLElement | null>(null);
const overview = ref<SystemMetricsOverview | null>(null);
const metricDetail = ref<MetricDetail | null>(null);
const metricTagOptions = ref<MetricAvailableTag[]>([]);
const metricNames = ref<string[]>([]);
const selectedMetric = ref(DEFAULT_METRIC);
const selectedTags = reactive<Record<string, string | undefined>>({});
const trendPoints = ref<TrendPoint[]>([]);
const initialLoading = ref(true);
const refreshing = ref(false);
const metricLoading = ref(false);
const isPaused = ref(false);
const connectionHealthy = ref(true);
const lastUpdatedAt = ref<number | null>(null);
const refreshInterval = ref(5);
const redisSnapshot = ref<RedisMonitorSnapshot | null>(null);
const redisTrendPoints = ref<RedisTrendPoint[]>([]);
const redisSearch = ref('');
const redisInitialLoading = ref(false);
const redisRefreshing = ref(false);
const redisConnectionHealthy = ref(true);
const redisLastUpdatedAt = ref<number | null>(null);

const refreshOptions = [
  { label: '5 秒', value: 5 },
  { label: '15 秒', value: 15 },
  { label: '30 秒', value: 30 },
];

const measurementColumns: PrimaryTableCol[] = [
  {
    title: '统计类型',
    colKey: 'statistic',
    width: 180,
  },
  {
    title: '当前值',
    colKey: 'value',
  },
  {
    title: '基础单位',
    colKey: 'unit',
    width: 180,
  },
];

const redisInfoColumns: PrimaryTableCol[] = [
  {
    title: '参数',
    colKey: 'key',
    width: 240,
  },
  {
    title: '当前值',
    colKey: 'value',
  },
  {
    title: '说明',
    colKey: 'description',
    width: 360,
  },
];

let cpuChart: echarts.ECharts | undefined;
let memoryChart: echarts.ECharts | undefined;
let redisMemoryChart: echarts.ECharts | undefined;
let redisOpsChart: echarts.ECharts | undefined;
let pollingTimer: ReturnType<typeof setInterval> | undefined;
let resizeObserver: ResizeObserver | undefined;
let hasMounted = false;
let isPageActive = true;
let errorReported = false;
let redisErrorReported = false;
let redisInitialized = false;
let detailRequestId = 0;

const metricOptions = computed(() => metricNames.value.map((name) => ({ label: name, value: name })));
const systemCpuPercent = computed(() => toPercentage(overview.value?.systemCpuUsage));
const processCpuPercent = computed(() => toPercentage(overview.value?.processCpuUsage));
const heapUsagePercent = computed(() =>
  calculateUsagePercentage(overview.value?.heapMemoryUsed, overview.value?.heapMemoryMax),
);
const diskUsed = computed(() => {
  const total = overview.value?.diskTotal;
  const free = overview.value?.diskFree;
  if (!isFiniteNumber(total) || !isFiniteNumber(free)) {
    return null;
  }
  return Math.max(total - free, 0);
});
const diskUsagePercent = computed(() => calculateUsagePercentage(diskUsed.value, overview.value?.diskTotal));
const activeRefreshing = computed(() => (activeTab.value === 'system' ? refreshing.value : redisRefreshing.value));
const activeConnectionHealthy = computed(() =>
  activeTab.value === 'system' ? connectionHealthy.value : redisConnectionHealthy.value,
);
const activeLastUpdatedAt = computed(() =>
  activeTab.value === 'system' ? lastUpdatedAt.value : redisLastUpdatedAt.value,
);
const lastUpdatedText = computed(() =>
  activeLastUpdatedAt.value === null ? '--' : dayjs(activeLastUpdatedAt.value).format('YYYY-MM-DD HH:mm:ss'),
);
const statusText = computed(() => {
  if (isPaused.value) return '已暂停';
  if (activeRefreshing.value) return '刷新中';
  return activeConnectionHealthy.value ? '运行正常' : '连接异常';
});
const statusTheme = computed<TagProps['theme']>(() => {
  if (isPaused.value) return 'warning';
  if (!activeConnectionHealthy.value) return 'danger';
  return 'success';
});
const descriptionColumns = computed(() => (viewportWidth.value < 900 ? 1 : 3));
const measurementRows = computed<MeasurementRow[]>(() => {
  if (!metricDetail.value) return [];
  return metricDetail.value.measurements.map((measurement) => ({
    ...measurement,
    displayValue: formatMetricValue(measurement.value, metricDetail.value?.baseUnit, metricDetail.value?.name),
    unit: metricDetail.value?.baseUnit || '无',
  }));
});
const redisIdentityText = computed(
  () => [redisSnapshot.value?.redisMode, redisSnapshot.value?.role].filter(Boolean).join(' / ') || '--',
);
const redisMemoryUsagePercent = computed(() =>
  calculateUsagePercentage(redisSnapshot.value?.usedMemory, redisSnapshot.value?.maxMemory),
);
const redisHitRatePercent = computed(() => toPercentage(redisSnapshot.value?.keyspaceHitRate));
const redisMemoryMeta = computed(() => {
  const snapshot = redisSnapshot.value;
  if (!snapshot) return '--';
  if (isFiniteNumber(snapshot.maxMemory) && snapshot.maxMemory > 0) {
    return `上限 ${formatBytes(snapshot.maxMemory)}`;
  }
  const peak = `峰值 ${formatBytes(snapshot.peakMemory)}`;
  return snapshot.maxMemory === 0 ? `${peak} · 未限制` : peak;
});
const filteredRedisDetails = computed(() => {
  const keyword = redisSearch.value.trim().toLocaleLowerCase();
  const details = redisSnapshot.value?.details ?? [];
  if (!keyword) return details;
  return details.filter((item) =>
    [item.key, item.value, item.description ?? ''].some((value) => value.toLocaleLowerCase().includes(keyword)),
  );
});

function isFiniteNumber(value: number | null | undefined): value is number {
  return typeof value === 'number' && Number.isFinite(value);
}

function toPercentage(value: number | null | undefined) {
  if (!isFiniteNumber(value)) return null;
  return Math.min(Math.max(value * 100, 0), 100);
}

function calculateUsagePercentage(used: number | null | undefined, total: number | null | undefined) {
  if (!isFiniteNumber(used) || !isFiniteNumber(total) || total <= 0) return null;
  return Math.min(Math.max((used / total) * 100, 0), 100);
}

function formatBytes(value: number | null | undefined) {
  if (!isFiniteNumber(value) || value < 0) return '--';
  if (value === 0) return '0 B';
  const units = ['B', 'KiB', 'MiB', 'GiB', 'TiB'];
  const unitIndex = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1);
  const converted = value / 1024 ** unitIndex;
  return `${converted.toFixed(converted >= 100 ? 0 : converted >= 10 ? 1 : 2)} ${units[unitIndex]}`;
}

function formatCount(value: number | null | undefined) {
  if (!isFiniteNumber(value)) return '--';
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 0 }).format(value);
}

function formatDuration(value: number | null | undefined) {
  if (!isFiniteNumber(value) || value < 0) return '--';
  const totalSeconds = Math.floor(value);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  if (days > 0) return `${days} 天 ${hours} 小时`;
  if (hours > 0) return `${hours} 小时 ${minutes} 分`;
  return `${minutes} 分 ${totalSeconds % 60} 秒`;
}

function formatMetricValue(value: number, unit: string | null | undefined, metricName: string | undefined) {
  if (!Number.isFinite(value)) return '--';
  if (unit === 'bytes') return formatBytes(value);
  if (unit === 'seconds' && metricName === 'process.uptime') return formatDuration(value);
  if (!unit && metricName?.endsWith('.usage') && value >= 0 && value <= 1) {
    return `${(value * 100).toFixed(2)}%`;
  }
  return new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 4 }).format(value);
}

function progressColor(percentage: number) {
  if (percentage >= 85) return 'var(--td-error-color)';
  if (percentage >= 70) return 'var(--td-warning-color)';
  return 'var(--td-success-color)';
}

function appendTrendPoint(data: SystemMetricsOverview) {
  trendPoints.value.push({
    time: dayjs(data.timestamp).format('HH:mm:ss'),
    systemCpu: toPercentage(data.systemCpuUsage),
    processCpu: toPercentage(data.processCpuUsage),
    heapUsage: calculateUsagePercentage(data.heapMemoryUsed, data.heapMemoryMax),
  });
  if (trendPoints.value.length > MAX_TREND_POINTS) {
    trendPoints.value.splice(0, trendPoints.value.length - MAX_TREND_POINTS);
  }
}

function appendRedisTrendPoint(data: RedisMonitorSnapshot) {
  redisTrendPoints.value.push({
    time: dayjs(data.timestamp).format('HH:mm:ss'),
    usedMemory: isFiniteNumber(data.usedMemory) ? data.usedMemory : null,
    operationsPerSecond: isFiniteNumber(data.operationsPerSecond) ? data.operationsPerSecond : null,
  });
  if (redisTrendPoints.value.length > MAX_TREND_POINTS) {
    redisTrendPoints.value.splice(0, redisTrendPoints.value.length - MAX_TREND_POINTS);
  }
}

function getCssColor(name: string, fallback: string) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback;
}

function updateChartOptions() {
  const times = trendPoints.value.map((item) => item.time);
  const textColor = settingStore.chartColors.textColor;
  const borderColor = settingStore.chartColors.borderColor;
  const brandColor = getCssColor('--td-brand-color', '#0052d9');
  const successColor = getCssColor('--td-success-color', '#00a870');
  const warningColor = getCssColor('--td-warning-color', '#ed7b2f');
  const commonOption = {
    animationDuration: 300,
    grid: {
      top: 48,
      right: 20,
      bottom: 28,
      left: 48,
    },
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value: number) => (isFiniteNumber(value) ? `${value.toFixed(2)}%` : '--'),
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLine: { lineStyle: { color: borderColor } },
      axisLabel: { color: textColor },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: {
        color: textColor,
        formatter: '{value}%',
      },
      splitLine: { lineStyle: { color: borderColor, type: 'dashed' } },
    },
  };

  cpuChart?.setOption(
    {
      ...commonOption,
      color: [brandColor, successColor],
      legend: {
        data: ['系统 CPU', '进程 CPU'],
        top: 0,
        textStyle: { color: textColor },
      },
      series: [
        {
          name: '系统 CPU',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: trendPoints.value.map((item) => item.systemCpu),
        },
        {
          name: '进程 CPU',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: trendPoints.value.map((item) => item.processCpu),
        },
      ],
    },
    true,
  );

  memoryChart?.setOption(
    {
      ...commonOption,
      color: [warningColor],
      legend: {
        data: ['堆内存使用率'],
        top: 0,
        textStyle: { color: textColor },
      },
      series: [
        {
          name: '堆内存使用率',
          type: 'line',
          smooth: true,
          showSymbol: false,
          areaStyle: { opacity: 0.08 },
          data: trendPoints.value.map((item) => item.heapUsage),
        },
      ],
    },
    true,
  );
}

function updateRedisChartOptions() {
  const times = redisTrendPoints.value.map((item) => item.time);
  const textColor = settingStore.chartColors.textColor;
  const borderColor = settingStore.chartColors.borderColor;
  const brandColor = getCssColor('--td-brand-color', '#0052d9');
  const successColor = getCssColor('--td-success-color', '#00a870');
  const commonAxis = {
    animationDuration: 300,
    grid: {
      top: 48,
      right: 20,
      bottom: 28,
      left: 72,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLine: { lineStyle: { color: borderColor } },
      axisLabel: { color: textColor },
    },
  };

  redisMemoryChart?.setOption(
    {
      ...commonAxis,
      color: [brandColor],
      tooltip: {
        trigger: 'axis',
        valueFormatter: (value: number) => formatBytes(value),
      },
      yAxis: {
        type: 'value',
        min: 0,
        axisLabel: {
          color: textColor,
          formatter: (value: number) => formatBytes(value),
        },
        splitLine: { lineStyle: { color: borderColor, type: 'dashed' } },
      },
      series: [
        {
          name: '已用内存',
          type: 'line',
          smooth: true,
          showSymbol: false,
          areaStyle: { opacity: 0.08 },
          data: redisTrendPoints.value.map((item) => item.usedMemory),
        },
      ],
    },
    true,
  );

  redisOpsChart?.setOption(
    {
      ...commonAxis,
      color: [successColor],
      tooltip: {
        trigger: 'axis',
        valueFormatter: (value: number) => `${formatCount(value)} ops/s`,
      },
      yAxis: {
        type: 'value',
        min: 0,
        axisLabel: { color: textColor },
        splitLine: { lineStyle: { color: borderColor, type: 'dashed' } },
      },
      series: [
        {
          name: '实时吞吐',
          type: 'line',
          smooth: true,
          showSymbol: false,
          areaStyle: { opacity: 0.08 },
          data: redisTrendPoints.value.map((item) => item.operationsPerSecond),
        },
      ],
    },
    true,
  );
}

function initCharts() {
  if (cpuChartContainer.value && !cpuChart) {
    cpuChart = echarts.init(cpuChartContainer.value);
  }
  if (memoryChartContainer.value && !memoryChart) {
    memoryChart = echarts.init(memoryChartContainer.value);
  }
  updateChartOptions();
}

function initRedisCharts() {
  if (redisMemoryChartContainer.value && !redisMemoryChart) {
    redisMemoryChart = echarts.init(redisMemoryChartContainer.value);
  }
  if (redisOpsChartContainer.value && !redisOpsChart) {
    redisOpsChart = echarts.init(redisOpsChartContainer.value);
  }
  updateRedisChartOptions();
}

function resizeCharts() {
  cpuChart?.resize();
  memoryChart?.resize();
  redisMemoryChart?.resize();
  redisOpsChart?.resize();
}

function disposeCharts() {
  cpuChart?.dispose();
  memoryChart?.dispose();
  redisMemoryChart?.dispose();
  redisOpsChart?.dispose();
  cpuChart = undefined;
  memoryChart = undefined;
  redisMemoryChart = undefined;
  redisOpsChart = undefined;
}

function observeChartSize() {
  resizeObserver?.disconnect();
  if (typeof ResizeObserver === 'undefined') return;
  resizeObserver = new ResizeObserver(() => resizeCharts());
  if (cpuChartContainer.value) resizeObserver.observe(cpuChartContainer.value);
  if (memoryChartContainer.value) resizeObserver.observe(memoryChartContainer.value);
  if (redisMemoryChartContainer.value) resizeObserver.observe(redisMemoryChartContainer.value);
  if (redisOpsChartContainer.value) resizeObserver.observe(redisOpsChartContainer.value);
}

function clearSelectedTags() {
  Object.keys(selectedTags).forEach((key) => delete selectedTags[key]);
}

async function loadMetricDetail(resetTagOptions = false, showLoading = true) {
  if (!selectedMetric.value) {
    metricDetail.value = null;
    metricTagOptions.value = [];
    return false;
  }
  const requestId = ++detailRequestId;
  const metricName = selectedMetric.value;
  const tags = resetTagOptions ? {} : { ...selectedTags };
  if (showLoading) metricLoading.value = true;

  try {
    const detail = await getMetricDetail(metricName, tags);
    if (requestId !== detailRequestId || metricName !== selectedMetric.value) return true;
    metricDetail.value = detail;
    if (resetTagOptions) {
      metricTagOptions.value = detail.availableTags;
    }
    return true;
  } catch (error) {
    if (requestId === detailRequestId && metricName === selectedMetric.value) {
      metricDetail.value = null;
    }
    console.error('加载指标详情失败', error);
    if (showLoading) {
      void MessagePlugin.error('加载指标详情失败');
    }
    return false;
  } finally {
    if (showLoading && requestId === detailRequestId) {
      metricLoading.value = false;
    }
  }
}

function reportRefreshError(error: unknown) {
  connectionHealthy.value = false;
  console.error('刷新系统指标失败', error);
  if (!errorReported) {
    errorReported = true;
    void MessagePlugin.error('刷新系统指标失败');
  }
}

function markRefreshSuccess(timestamp: number) {
  connectionHealthy.value = true;
  errorReported = false;
  lastUpdatedAt.value = timestamp;
}

function reportRedisRefreshError(error: unknown) {
  redisConnectionHealthy.value = false;
  console.error('刷新 Redis 指标失败', error);
  if (!redisErrorReported) {
    redisErrorReported = true;
    void MessagePlugin.error('刷新 Redis 指标失败');
  }
}

function markRedisRefreshSuccess(timestamp: number) {
  redisConnectionHealthy.value = true;
  redisErrorReported = false;
  redisLastUpdatedAt.value = timestamp;
}

async function initializeData() {
  initialLoading.value = true;
  const [overviewResult, namesResult, detailResult] = await Promise.allSettled([
    getSystemMetricsOverview(),
    getMetricNames(),
    getMetricDetail(DEFAULT_METRIC),
  ]);

  let hasError = false;
  if (overviewResult.status === 'fulfilled') {
    overview.value = overviewResult.value;
    appendTrendPoint(overviewResult.value);
    markRefreshSuccess(overviewResult.value.timestamp);
  } else {
    hasError = true;
    reportRefreshError(overviewResult.reason);
  }

  if (namesResult.status === 'fulfilled') {
    metricNames.value = namesResult.value.names;
    selectedMetric.value = metricNames.value.includes(DEFAULT_METRIC) ? DEFAULT_METRIC : metricNames.value[0] || '';
  } else {
    hasError = true;
    reportRefreshError(namesResult.reason);
  }

  if (selectedMetric.value === DEFAULT_METRIC && detailResult.status === 'fulfilled') {
    metricDetail.value = detailResult.value;
    metricTagOptions.value = detailResult.value.availableTags;
  } else if (selectedMetric.value) {
    hasError = !(await loadMetricDetail(true, false)) || hasError;
  } else {
    metricDetail.value = null;
    metricTagOptions.value = [];
  }

  if (detailResult.status === 'rejected' && selectedMetric.value === DEFAULT_METRIC) {
    hasError = true;
    console.error('加载默认指标失败', detailResult.reason);
  }

  connectionHealthy.value = !hasError;
  if (hasError && !errorReported) {
    reportRefreshError(new Error('部分系统指标加载失败'));
  }
  initialLoading.value = false;
  await nextTick();
  initCharts();
}

async function initializeRedisData() {
  if (redisInitialized || redisInitialLoading.value || redisRefreshing.value) return;
  redisInitialized = true;
  redisInitialLoading.value = true;

  try {
    const snapshot = await getRedisMonitorSnapshot();
    redisSnapshot.value = snapshot;
    appendRedisTrendPoint(snapshot);
    markRedisRefreshSuccess(snapshot.timestamp);
  } catch (error) {
    reportRedisRefreshError(error);
  } finally {
    redisInitialLoading.value = false;
  }

  if (activeTab.value === 'redis') {
    await nextTick();
    initRedisCharts();
    observeChartSize();
  }
}

async function refreshData() {
  if (refreshing.value) return;
  refreshing.value = true;
  const metricName = selectedMetric.value;
  const tags = { ...selectedTags };
  const requests: [ReturnType<typeof getSystemMetricsOverview>, Promise<MetricDetail | null>] = [
    getSystemMetricsOverview(),
    metricName ? getMetricDetail(metricName, tags) : Promise.resolve(null),
  ];

  try {
    const [overviewResult, detailResult] = await Promise.allSettled(requests);
    let hasError = false;

    if (overviewResult.status === 'fulfilled') {
      overview.value = overviewResult.value;
      appendTrendPoint(overviewResult.value);
      lastUpdatedAt.value = overviewResult.value.timestamp;
    } else {
      hasError = true;
      reportRefreshError(overviewResult.reason);
    }

    if (detailResult.status === 'fulfilled') {
      if (detailResult.value && metricName === selectedMetric.value) {
        metricDetail.value = detailResult.value;
      }
    } else {
      hasError = true;
      reportRefreshError(detailResult.reason);
    }

    if (!hasError && overviewResult.status === 'fulfilled') {
      markRefreshSuccess(overviewResult.value.timestamp);
    }
    updateChartOptions();
  } finally {
    refreshing.value = false;
  }
}

async function refreshRedisData() {
  if (redisRefreshing.value || redisInitialLoading.value) return;
  redisRefreshing.value = true;

  try {
    const snapshot = await getRedisMonitorSnapshot();
    redisSnapshot.value = snapshot;
    appendRedisTrendPoint(snapshot);
    markRedisRefreshSuccess(snapshot.timestamp);
    updateRedisChartOptions();
  } catch (error) {
    reportRedisRefreshError(error);
  } finally {
    redisRefreshing.value = false;
  }
}

async function refreshActiveData() {
  if (activeTab.value === 'system') {
    await refreshData();
    return;
  }
  if (redisInitialized) {
    await refreshRedisData();
  } else {
    await initializeRedisData();
  }
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = undefined;
  }
}

function startPolling() {
  stopPolling();
  if (isPaused.value || !isPageActive || document.hidden) return;
  pollingTimer = setInterval(() => {
    void refreshActiveData();
  }, refreshInterval.value * 1000);
}

function togglePolling() {
  isPaused.value = !isPaused.value;
  if (isPaused.value) {
    stopPolling();
  } else {
    void refreshActiveData();
    startPolling();
  }
}

function handleMetricChange() {
  clearSelectedTags();
  metricTagOptions.value = [];
  void loadMetricDetail(true);
}

function handleTagChange(tag: string, value: SelectValue) {
  if (typeof value === 'string' && value) {
    selectedTags[tag] = value;
  } else {
    delete selectedTags[tag];
  }
  void loadMetricDetail(false);
}

function handleVisibilityChange() {
  if (document.hidden) {
    stopPolling();
  } else if (!isPaused.value && isPageActive) {
    void refreshActiveData();
    startPolling();
  }
}

watch(refreshInterval, () => {
  startPolling();
});

watch(activeTab, async (tab) => {
  stopPolling();
  await nextTick();
  if (!isPageActive || document.hidden || tab !== activeTab.value) return;

  if (tab === 'system') {
    initCharts();
  } else {
    initRedisCharts();
  }
  observeChartSize();
  resizeCharts();
  await refreshActiveData();
  if (tab === activeTab.value) {
    startPolling();
  }
});

watch(
  () => [settingStore.mode, settingStore.brandTheme],
  () => {
    nextTick(() => {
      updateChartOptions();
      updateRedisChartOptions();
    });
  },
);

onMounted(async () => {
  hasMounted = true;
  document.addEventListener('visibilitychange', handleVisibilityChange);
  await initializeData();
  observeChartSize();
  startPolling();
});

onActivated(() => {
  if (!hasMounted || isPageActive) return;
  isPageActive = true;
  nextTick(() => {
    if (activeTab.value === 'system') {
      initCharts();
    } else {
      initRedisCharts();
    }
    observeChartSize();
    resizeCharts();
    void refreshActiveData();
    startPolling();
  });
});

onDeactivated(() => {
  isPageActive = false;
  stopPolling();
  resizeObserver?.disconnect();
  disposeCharts();
});

onUnmounted(() => {
  hasMounted = false;
  stopPolling();
  resizeObserver?.disconnect();
  disposeCharts();
  document.removeEventListener('visibilitychange', handleVisibilityChange);
});
</script>
<style lang="less" scoped>
.system-info {
  display: flex;
  flex-direction: column;
  gap: var(--td-comp-margin-m);
}

.monitor-toolbar {
  min-height: 56px;
  padding: var(--td-comp-paddingTB-m) var(--td-comp-paddingLR-l);
  border: 1px solid var(--td-component-border);
  border-radius: var(--td-radius-medium);
  background: var(--td-bg-color-container);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--td-comp-margin-m);
}

.monitor-status {
  display: flex;
  align-items: center;
  gap: var(--td-comp-margin-m);
  min-width: 0;

  &__time {
    color: var(--td-text-color-secondary);
    white-space: nowrap;
  }
}

.refresh-label {
  color: var(--td-text-color-secondary);
  white-space: nowrap;
}

.refresh-select {
  width: 104px;
}

.monitor-tabs {
  min-width: 0;

  :deep(.t-tabs__content) {
    overflow: visible;
    background: transparent;
  }
}

.monitor-tab-content {
  min-width: 0;
  padding-top: var(--td-comp-paddingTB-l);
  display: flex;
  flex-direction: column;
  gap: var(--td-comp-margin-m);
}

.overview-grid,
.trend-grid {
  width: 100%;
}

.metric-card {
  height: 176px;

  :deep(.t-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: var(--td-comp-paddingTB-l) var(--td-comp-paddingLR-l);
  }

  :deep(.t-statistic) {
    margin-top: var(--td-comp-margin-m);
  }

  :deep(.t-statistic__content-value) {
    color: var(--td-text-color-primary);
    font-size: var(--td-font-size-headline-medium);
    line-height: var(--td-line-height-headline-medium);
  }

  &__header {
    display: flex;
    align-items: center;
    gap: var(--td-comp-margin-s);
    color: var(--td-text-color-secondary);
    font: var(--td-font-body-medium);
  }

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: var(--td-radius-medium);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;
    font-size: 18px;

    &--brand {
      color: var(--td-brand-color);
      background: var(--td-brand-color-light);
    }

    &--success {
      color: var(--td-success-color);
      background: var(--td-success-color-1);
    }

    &--warning {
      color: var(--td-warning-color);
      background: var(--td-warning-color-1);
    }

    &--danger {
      color: var(--td-error-color);
      background: var(--td-error-color-1);
    }
  }

  &__value,
  &__empty {
    margin-top: var(--td-comp-margin-m);
    color: var(--td-text-color-primary);
    font-size: var(--td-font-size-headline-medium);
    line-height: var(--td-line-height-headline-medium);
    font-weight: 500;
    white-space: nowrap;
  }

  &__value--duration {
    font-size: 24px;
  }

  &__value--text {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__empty {
    color: var(--td-text-color-placeholder);
  }

  &__progress {
    margin-top: var(--td-comp-margin-s);
  }

  &__meta {
    margin-top: auto;
    color: var(--td-text-color-placeholder);
    font: var(--td-font-body-small);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.panel-card {
  padding: var(--td-comp-paddingTB-l) var(--td-comp-paddingLR-l);

  :deep(.t-card__header),
  :deep(.t-card__body) {
    padding: 0;
  }

  :deep(.t-card__title) {
    color: var(--td-text-color-primary);
    font: var(--td-font-title-medium);
  }

  :deep(.t-card__body) {
    margin-top: var(--td-comp-margin-l);
  }
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.metric-selector {
  width: min(360px, 40vw);
}

.redis-search {
  width: min(320px, 40vw);
}

.tag-filter-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--td-comp-margin-m);
  margin-top: var(--td-comp-margin-l);
}

.tag-filter {
  min-width: 0;

  &__label {
    display: block;
    margin-bottom: var(--td-comp-margin-xs);
    color: var(--td-text-color-secondary);
    font: var(--td-font-body-small);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.measurement-table {
  margin-top: var(--td-comp-margin-l);
}

.measurement-value {
  color: var(--td-text-color-primary);
  font-family: Consolas, Monaco, monospace;
  font-variant-numeric: tabular-nums;
}

.redis-info {
  &__value,
  &__description {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__value {
    color: var(--td-text-color-primary);
    font-family: Consolas, Monaco, monospace;
    font-variant-numeric: tabular-nums;
  }

  &__description {
    color: var(--td-text-color-secondary);
  }
}

@media (width <= 768px) {
  .monitor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .monitor-toolbar :deep(.t-space) {
    width: 100%;
    flex-wrap: wrap;
  }

  .metric-selector {
    width: 240px;
    max-width: 48vw;
  }

  .redis-search {
    width: 220px;
    max-width: 48vw;
  }
}
</style>
