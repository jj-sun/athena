package com.athena.modules.monitor.service.impl;

import com.athena.modules.monitor.domain.RedisMonitorSnapshot;
import com.athena.modules.monitor.exception.RedisConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

	@Mock
	private RedisConnectionFactory redisConnectionFactory;

	@Mock
	private RedisConnection redisConnection;

	@InjectMocks
	private RedisServiceImpl service;

	@BeforeEach
	void setUp() {
		when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
	}

	@Test
	void shouldBuildMonitorSnapshotAndSortDetails() throws Exception {
		Properties info = new Properties();
		info.setProperty("redis_version", "7.2.5");
		info.setProperty("redis_mode", "standalone");
		info.setProperty("role", "master");
		info.setProperty("uptime_in_seconds", "3600");
		info.setProperty("used_memory", "1048576");
		info.setProperty("used_memory_peak", "2097152");
		info.setProperty("maxmemory", "0");
		info.setProperty("connected_clients", "8");
		info.setProperty("blocked_clients", "1");
		info.setProperty("instantaneous_ops_per_sec", "24");
		info.setProperty("total_commands_processed", "1000");
		info.setProperty("keyspace_hits", "80");
		info.setProperty("keyspace_misses", "20");
		info.setProperty("rejected_connections", "2");
		when(redisConnection.info()).thenReturn(info);
		when(redisConnection.dbSize()).thenReturn(42L);

		RedisMonitorSnapshot snapshot = service.getMonitorSnapshot();

		assertThat(snapshot.getTimestamp()).isPositive();
		assertThat(snapshot.getRedisVersion()).isEqualTo("7.2.5");
		assertThat(snapshot.getRedisMode()).isEqualTo("standalone");
		assertThat(snapshot.getRole()).isEqualTo("master");
		assertThat(snapshot.getUptimeSeconds()).isEqualTo(3600);
		assertThat(snapshot.getKeyCount()).isEqualTo(42);
		assertThat(snapshot.getUsedMemory()).isEqualTo(1048576);
		assertThat(snapshot.getPeakMemory()).isEqualTo(2097152);
		assertThat(snapshot.getMaxMemory()).isZero();
		assertThat(snapshot.getConnectedClients()).isEqualTo(8);
		assertThat(snapshot.getBlockedClients()).isEqualTo(1);
		assertThat(snapshot.getOperationsPerSecond()).isEqualTo(24);
		assertThat(snapshot.getTotalCommandsProcessed()).isEqualTo(1000);
		assertThat(snapshot.getKeyspaceHitRate()).isEqualTo(0.8);
		assertThat(snapshot.getRejectedConnections()).isEqualTo(2);
		assertThat(snapshot.getDetails())
				.extracting("key")
				.isSorted();
		verify(redisConnection).close();
	}

	@Test
	void shouldReturnNullForMissingInvalidAndEmptyHitRateValues() throws Exception {
		Properties info = new Properties();
		info.setProperty("used_memory", "invalid");
		info.setProperty("keyspace_hits", "0");
		info.setProperty("keyspace_misses", "0");
		when(redisConnection.info()).thenReturn(info);

		RedisMonitorSnapshot snapshot = service.getMonitorSnapshot();

		assertThat(snapshot.getRedisVersion()).isNull();
		assertThat(snapshot.getUsedMemory()).isNull();
		assertThat(snapshot.getKeyspaceHitRate()).isNull();
		assertThat(snapshot.getDetails()).hasSize(3);
		verify(redisConnection).close();
	}

	@Test
	void shouldCloseConnectionAndWrapConnectionFailure() {
		RuntimeException failure = new RuntimeException("offline");
		when(redisConnection.info()).thenThrow(failure);

		assertThatThrownBy(service::getMonitorSnapshot)
				.isInstanceOf(RedisConnectException.class)
				.hasMessage("Redis 连接失败")
				.hasCause(failure);
		verify(redisConnection).close();
	}

}
