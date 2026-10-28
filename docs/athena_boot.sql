-- MySQL dump 10.13  Distrib 8.1.0, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: athena_boot
-- ------------------------------------------------------
-- Server version	8.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `athena_boot`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `athena_boot` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `athena_boot`;

--
-- Table structure for table `qrtz_blob_triggers`
--

DROP TABLE IF EXISTS `qrtz_blob_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_blob_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_blob_triggers`
--

LOCK TABLES `qrtz_blob_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_blob_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_blob_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_calendars`
--

DROP TABLE IF EXISTS `qrtz_calendars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_calendars` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_calendars`
--

LOCK TABLES `qrtz_calendars` WRITE;
/*!40000 ALTER TABLE `qrtz_calendars` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_calendars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_cron_triggers`
--

DROP TABLE IF EXISTS `qrtz_cron_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_cron_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_cron_triggers`
--

LOCK TABLES `qrtz_cron_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_cron_triggers` DISABLE KEYS */;
INSERT INTO `qrtz_cron_triggers` VALUES ('MyScheduler','TASK_1491736261201805313','DEFAULT','0 0/30 * * * ?','Asia/Shanghai');
/*!40000 ALTER TABLE `qrtz_cron_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_fired_triggers`
--

DROP TABLE IF EXISTS `qrtz_fired_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_fired_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_fired_triggers`
--

LOCK TABLES `qrtz_fired_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_fired_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_fired_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_job_details`
--

DROP TABLE IF EXISTS `qrtz_job_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_job_details` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_job_details`
--

LOCK TABLES `qrtz_job_details` WRITE;
/*!40000 ALTER TABLE `qrtz_job_details` DISABLE KEYS */;
INSERT INTO `qrtz_job_details` VALUES ('MyScheduler','TASK_1491736261201805313','DEFAULT',NULL,'com.athena.modules.job.utils.ScheduleJob','0','0','0','0',_binary '�\�\0sr\0org.quartz.JobDataMap���迩�\�\0\0xr\0&org.quartz.utils.StringKeyDirtyFlagMap�\�\��\�](\0Z\0allowsTransientDataxr\0org.quartz.utils.DirtyFlagMap\�.�(v\n\�\0Z\0dirtyL\0mapt\0Ljava/util/Map;xpsr\0java.util.HashMap\��\�`\�\0F\0\nloadFactorI\0	thresholdxp?@\0\0\0\0\0w\0\0\0\0\0\0t\0\rJOB_PARAM_KEYsr\0)com.athena.modules.job.entity.ScheduleJob\0\0\0\0\0\0\0\0L\0beanNamet\0Ljava/lang/String;L\0cronExpressionq\0~\0	L\0	parameterq\0~\0	L\0remarkq\0~\0	L\0statust\0Ljava/lang/Integer;xr\0 com.athena.common.base.po.BasePo��\�3q�\0L\0creatorq\0~\0	L\0ctimet\0Ljava/util/Date;L\0delFlagq\0~\0\nL\0editorq\0~\0	L\0idq\0~\0	L\0mtimeq\0~\0xpt\0adminsr\0java.util.Datehj�KYt\0\0xpw\0\0~\�e\�/xsr\0java.lang.Integer⠤\���8\0I\0valuexr\0java.lang.Number����\��\0\0xp\0\0\0\0q\0~\0t\01491736261201805313sq\0~\0w\0\0~\�e\�1xt\0testTaskt\00 0/30 * * * ?t\0athenat\0%有参测试，多个参数使用jsonq\0~\0x\0');
/*!40000 ALTER TABLE `qrtz_job_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_locks`
--

DROP TABLE IF EXISTS `qrtz_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_locks` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_locks`
--

LOCK TABLES `qrtz_locks` WRITE;
/*!40000 ALTER TABLE `qrtz_locks` DISABLE KEYS */;
INSERT INTO `qrtz_locks` VALUES ('MyScheduler','TRIGGER_ACCESS');
/*!40000 ALTER TABLE `qrtz_locks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_paused_trigger_grps`
--

DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_paused_trigger_grps`
--

LOCK TABLES `qrtz_paused_trigger_grps` WRITE;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_paused_trigger_grps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_scheduler_state`
--

DROP TABLE IF EXISTS `qrtz_scheduler_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_scheduler_state` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_scheduler_state`
--

LOCK TABLES `qrtz_scheduler_state` WRITE;
/*!40000 ALTER TABLE `qrtz_scheduler_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_scheduler_state` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simple_triggers`
--

DROP TABLE IF EXISTS `qrtz_simple_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simple_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simple_triggers`
--

LOCK TABLES `qrtz_simple_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simple_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simple_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_simprop_triggers`
--

DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_simprop_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_simprop_triggers`
--

LOCK TABLES `qrtz_simprop_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` DISABLE KEYS */;
/*!40000 ALTER TABLE `qrtz_simprop_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `qrtz_triggers`
--

DROP TABLE IF EXISTS `qrtz_triggers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qrtz_triggers` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `qrtz_job_details` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qrtz_triggers`
--

LOCK TABLES `qrtz_triggers` WRITE;
/*!40000 ALTER TABLE `qrtz_triggers` DISABLE KEYS */;
INSERT INTO `qrtz_triggers` VALUES ('MyScheduler','TASK_1491736261201805313','DEFAULT','TASK_1491736261201805313','DEFAULT',NULL,1644494400000,-1,5,'PAUSED','CRON',1644492614000,0,NULL,2,_binary '�\�\0sr\0org.quartz.JobDataMap���迩�\�\0\0xr\0&org.quartz.utils.StringKeyDirtyFlagMap�\�\��\�](\0Z\0allowsTransientDataxr\0org.quartz.utils.DirtyFlagMap\�.�(v\n\�\0Z\0dirtyL\0mapt\0Ljava/util/Map;xpsr\0java.util.HashMap\��\�`\�\0F\0\nloadFactorI\0	thresholdxp?@\0\0\0\0\0w\0\0\0\0\0\0t\0\rJOB_PARAM_KEYsr\0)com.athena.modules.job.entity.ScheduleJob\0\0\0\0\0\0\0\0L\0beanNamet\0Ljava/lang/String;L\0cronExpressionq\0~\0	L\0	parameterq\0~\0	L\0remarkq\0~\0	L\0statust\0Ljava/lang/Integer;xr\0 com.athena.common.base.po.BasePo��\�3q�\0L\0creatorq\0~\0	L\0ctimet\0Ljava/util/Date;L\0delFlagq\0~\0\nL\0editorq\0~\0	L\0idq\0~\0	L\0mtimeq\0~\0xpt\0adminsr\0java.util.Datehj�KYt\0\0xpw\0\0~\�e\�pxsr\0java.lang.Integer⠤\���8\0I\0valuexr\0java.lang.Number����\��\0\0xp\0\0\0\0t\0admint\01491736261201805313sq\0~\0w\0\0~\�e\�pxt\0testTaskt\00 0/30 * * * ?t\0athenat\0%有参测试，多个参数使用jsonsq\0~\0\0\0\0x\0');
/*!40000 ALTER TABLE `qrtz_triggers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule_job`
--

DROP TABLE IF EXISTS `schedule_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_job` (
  `id` varchar(32) NOT NULL COMMENT '任务id',
  `bean_name` varchar(200) DEFAULT NULL COMMENT 'spring bean名称',
  `parameter` varchar(2000) DEFAULT NULL COMMENT '参数',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'cron表达式',
  `status` tinyint DEFAULT NULL COMMENT '任务状态  0：正常  1：暂停',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `mtime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
  `editor` varchar(32) DEFAULT NULL COMMENT '修改人',
  `del_flag` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule_job`
--

LOCK TABLES `schedule_job` WRITE;
/*!40000 ALTER TABLE `schedule_job` DISABLE KEYS */;
INSERT INTO `schedule_job` VALUES ('1491736261201805313','testTask','athena','0 0/30 * * * ?',1,'有参测试，多个参数使用json','2022-02-10 19:30:14','2022-02-10 19:30:14','admin','admin',0);
/*!40000 ALTER TABLE `schedule_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule_job_log`
--

DROP TABLE IF EXISTS `schedule_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_job_log` (
  `id` varchar(32) NOT NULL COMMENT '任务日志id',
  `job_id` varchar(32) NOT NULL COMMENT '任务id',
  `bean_name` varchar(200) DEFAULT NULL COMMENT 'spring bean名称',
  `parameter` varchar(2000) DEFAULT NULL COMMENT '参数',
  `status` tinyint NOT NULL COMMENT '任务状态    0：成功    1：失败',
  `error` varchar(2000) DEFAULT NULL COMMENT '失败信息',
  `times` int NOT NULL COMMENT '耗时(单位：毫秒)',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule_job_log`
--

LOCK TABLES `schedule_job_log` WRITE;
/*!40000 ALTER TABLE `schedule_job_log` DISABLE KEYS */;
INSERT INTO `schedule_job_log` VALUES ('1491741983658500097','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-10 19:52:59'),('1491743750479736834','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-10 20:00:00'),('1491743821585821697','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-10 20:00:17'),('1491751300222717953','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-10 20:30:00'),('1491758850020335618','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-10 21:00:00'),('1491955143405477889','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-11 10:00:00'),('1491962693198815234','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-11 10:30:00'),('1491970242832769026','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-11 11:00:00'),('1491977792638689281','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 11:30:00'),('1491985342369112065','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 12:00:00'),('1491992892149866497','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 12:30:00'),('1492000441901260801','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 13:00:00'),('1492007991627489282','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 13:30:00'),('1492015541345329154','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-11 14:00:00'),('1492023091180609537','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-11 14:30:00'),('1493019657781653505','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-14 08:30:00'),('1493027207453356033','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 09:00:00'),('1493034757242499073','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 09:30:00'),('1493049856904663042','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-14 10:30:00'),('1493057406496673793','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-14 11:00:00'),('1493064956176764930','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 11:30:00'),('1493072506024628226','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-14 12:00:00'),('1493080055675359233','1491736261201805313','testTask','athena',0,NULL,0,'2022-02-14 12:30:00'),('1493087605623885825','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 13:00:00'),('1493095155207507969','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 13:30:00'),('1493102705114091521','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 14:00:00'),('1493110254727073794','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 14:30:00'),('1493117804533108737','1491736261201805313','testTask','athena',0,NULL,2,'2022-02-14 15:00:00'),('1493125354217394178','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 15:30:00'),('1493132903964594177','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 16:00:00'),('1493140453770514433','1491736261201805313','testTask','athena',0,NULL,1,'2022-02-14 16:30:00'),('2087517171214843905','1491736261201805313','testTask','athena',0,NULL,1,'2026-08-12 20:30:52'),('2087517171277758465','1491736261201805313','testTask','athena',0,NULL,0,'2026-08-12 20:30:52'),('2087522418041683969','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-12 20:51:43'),('2087524500958216193','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-12 21:00:00'),('2087532050717999105','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',3,'2026-08-12 21:30:00'),('2087539600469393409','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-12 22:00:00'),('2087547150212399106','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-12 22:30:00'),('2087554699955404801','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-12 23:00:00'),('2087562249719382018','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',3,'2026-08-12 23:30:00'),('2087569799730823169','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',66,'2026-08-13 00:00:00'),('2087577349213782018','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',4,'2026-08-13 00:30:00'),('2087584898944204801','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',3,'2026-08-13 01:00:00'),('2087592448699793410','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 01:30:00'),('2087599998430216194','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',1,'2026-08-13 02:00:00'),('2087607548189999106','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',1,'2026-08-13 02:30:00'),('2087615097933004802','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 03:00:00'),('2087622647676010497','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 03:30:00'),('2087630197435793409','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 04:00:00'),('2087637747174604802','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 04:30:00'),('2087645296905027585','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',1,'2026-08-13 05:00:00'),('2087652846656421890','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 05:30:00'),('2087660396416204802','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 06:00:00'),('2087667946167599105','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',3,'2026-08-13 06:30:00'),('2087675495918993410','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',3,'2026-08-13 07:00:00'),('2087683045653610498','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 07:30:00'),('2087690595400810497','2087522224373891073','1111','1',1,'org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named \'1111\' available',2,'2026-08-13 08:00:00');
/*!40000 ALTER TABLE `schedule_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_captcha`
--

DROP TABLE IF EXISTS `sys_captcha`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_captcha` (
  `uuid` char(36) NOT NULL COMMENT 'uuid',
  `code` varchar(6) NOT NULL COMMENT '验证码',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统验证码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_captcha`
--

LOCK TABLES `sys_captcha` WRITE;
/*!40000 ALTER TABLE `sys_captcha` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_captcha` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` varchar(32) NOT NULL,
  `param_key` varchar(50) DEFAULT NULL COMMENT 'key',
  `param_value` varchar(2000) DEFAULT NULL COMMENT 'value',
  `status` tinyint DEFAULT '1' COMMENT '状态   0：显示   1：隐藏',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `param_key` (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES ('1','CLOUD_STORAGE_CONFIG_KEY','{\"aliyunAccessKeyId\":\"\",\"aliyunAccessKeySecret\":\"\",\"aliyunBucketName\":\"\",\"aliyunDomain\":\"\",\"aliyunEndPoint\":\"\",\"aliyunPrefix\":\"\",\"qcloudBucketName\":\"\",\"qcloudDomain\":\"\",\"qcloudPrefix\":\"\",\"qcloudSecretId\":\"\",\"qcloudSecretKey\":\"\",\"qiniuAccessKey\":\"NrgMfABZxWLo5B-YYSjoE8-AZ1EISdi1Z3ubLOeZ\",\"qiniuBucketName\":\"ios-app\",\"qiniuDomain\":\"http://7xqbwh.dl1.z0.glb.clouddn.com\",\"qiniuPrefix\":\"upload\",\"qiniuSecretKey\":\"uIwJHevMRWU0VLxFvgy0tAcOdGqasdtVlJkdy6vV\",\"type\":1}',0,'云存储配置信息');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `parent_id` varchar(36) DEFAULT NULL COMMENT '父机构ID',
  `dept_name` varchar(100) NOT NULL COMMENT '机构/部门名称',
  `dept_code` varchar(64) NOT NULL COMMENT '机构编码',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `del_flag` varchar(1) DEFAULT NULL COMMENT '删除状态（0，启用，1不启用）',
  `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
  `ctime` datetime DEFAULT NULL COMMENT '创建日期',
  `editor` varchar(32) DEFAULT NULL COMMENT '更新人',
  `mtime` datetime DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_depart_org_code` (`dept_code`) USING BTREE,
  KEY `index_depart_parent_id` (`parent_id`) USING BTREE,
  KEY `index_depart_depart_order` (`sort_order`) USING BTREE,
  KEY `index_depart_org_code` (`dept_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='组织机构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dept`
--

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES ('1485429618908086274','0','总公司','1001','',0,'0','admin','2022-01-24 09:49:54','admin','2022-01-24 09:49:54'),('1485429843877969921','1485429618908086274','鲁南分公司','1001001','',0,'0','admin','2022-01-24 09:50:47','admin','2022-02-08 11:28:29'),('1485429924664459265','1485429618908086274','鲁西分公司','1001002','',1,'0','admin','2022-01-24 09:51:07','admin','2022-01-24 09:51:07');
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict`
--

DROP TABLE IF EXISTS `sys_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict` (
  `id` varchar(32) NOT NULL,
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `type` int(1) unsigned zerofill DEFAULT '0' COMMENT '字典类型0为string,1为number',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `del_flag` int DEFAULT '0' COMMENT '删除状态',
  `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `editor` varchar(32) DEFAULT NULL COMMENT '更新人',
  `mtime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `indextable_dict_code` (`dict_code`) USING BTREE,
  UNIQUE KEY `uk_sd_dict_code` (`dict_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict`
--

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;
INSERT INTO `sys_dict` VALUES ('1174509082208395266','职务职级','position_rank',0,'职务表职级字典',0,'admin','2019-09-19 10:22:41',NULL,NULL),('1250687930947620866','定时任务状态','quartz_status',NULL,'',0,'admin','2020-04-16 15:30:14','',NULL),('3486f32803bb953e7155dab3513dc68b','删除状态','del_flag',0,NULL,0,'admin','2019-01-18 21:46:26','admin','2019-03-30 11:17:11'),('3d9a351be3436fbefb1307d4cfb49bf2','性别','sex',1,NULL,0,NULL,'2019-01-04 14:56:32','admin','2019-03-30 11:28:27'),('83bfb33147013cc81640d5fd9eda030c','日志类型','log_type',1,NULL,0,'admin','2019-03-18 23:22:19',NULL,NULL),('880a895c98afeca9d9ac39f29e67c13e','操作类型','operate_type',0,'操作类型',0,'admin','2019-07-22 10:54:29',NULL,NULL),('ac2f7c0c5c5775fcea7e2387bcb22f01','菜单类型','menu_type',1,NULL,0,'admin','2020-12-18 23:24:32','admin','2019-04-01 15:27:06');
/*!40000 ALTER TABLE `sys_dict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_item`
--

DROP TABLE IF EXISTS `sys_dict_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_item` (
  `id` varchar(32) NOT NULL,
  `dict_id` varchar(32) DEFAULT NULL COMMENT '字典id',
  `item_text` varchar(100) NOT NULL COMMENT '字典项文本',
  `item_value` varchar(100) NOT NULL COMMENT '字典项值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `del_flag` int DEFAULT '0' COMMENT '状态（0启用 1不启用）',
  `creator` varchar(32) DEFAULT NULL,
  `ctime` datetime DEFAULT NULL,
  `editor` varchar(32) DEFAULT NULL,
  `mtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_table_dict_id` (`dict_id`) USING BTREE,
  KEY `index_table_sort_order` (`sort_order`) USING BTREE,
  KEY `index_table_dict_status` (`del_flag`) USING BTREE,
  KEY `idx_sdi_role_dict_id` (`dict_id`) USING BTREE,
  KEY `idx_sdi_role_sort_order` (`sort_order`) USING BTREE,
  KEY `idx_sdi_status` (`del_flag`) USING BTREE,
  KEY `idx_sdi_dict_val` (`dict_id`,`item_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_item`
--

LOCK TABLES `sys_dict_item` WRITE;
/*!40000 ALTER TABLE `sys_dict_item` DISABLE KEYS */;
INSERT INTO `sys_dict_item` VALUES ('05a2e732ce7b00aa52141ecc3e330b4e','3486f32803bb953e7155dab3513dc68b','已删除','1',NULL,NULL,1,'admin','2025-10-18 21:46:56','admin','2019-03-28 22:23:20'),('1174509601047994369','1174509082208395266','员级','1','',1,1,'admin','2019-09-19 10:24:45','admin','2019-09-23 11:46:39'),('1174509667297026049','1174509082208395266','助级','2','',2,1,'admin','2019-09-19 10:25:01','admin','2019-09-23 11:46:47'),('1174509713568587777','1174509082208395266','中级','3','',3,1,'admin','2019-09-19 10:25:12','admin','2019-09-23 11:46:56'),('1174509788361416705','1174509082208395266','副高级','4','',4,1,'admin','2019-09-19 10:25:30','admin','2019-09-23 11:47:06'),('1174509835803189250','1174509082208395266','正高级','5','',5,1,'admin','2019-09-19 10:25:41','admin','2019-09-23 11:47:12'),('1250688147579228161','1250687930947620866','正常','0','',1,1,'admin','2020-04-16 15:31:05','',NULL),('1250688201064992770','1250687930947620866','停止','-1','',1,1,'admin','2020-04-16 15:31:18','',NULL),('2114966825630883842','3d9a351be3436fbefb1307d4cfb49bf2','其他','3',NULL,3,0,'admin','2026-10-27 14:26:00','admin','2026-10-27 14:26:00'),('4f05fb5376f4c61502c5105f52e4dd2b','83bfb33147013cc81640d5fd9eda030c','操作日志','2',NULL,NULL,1,'admin','2019-03-18 23:22:49',NULL,NULL),('5d833f69296f691843ccdd0c91212b6b','880a895c98afeca9d9ac39f29e67c13e','修改','3','',3,1,'admin','2019-07-22 10:55:07','admin','2019-07-22 10:55:41'),('5d84a8634c8fdfe96275385075b105c9','3d9a351be3436fbefb1307d4cfb49bf2','女','2',NULL,2,1,NULL,'2019-01-04 14:56:56',NULL,'2019-01-04 17:38:12'),('7ccf7b80c70ee002eceb3116854b75cb','ac2f7c0c5c5775fcea7e2387bcb22f01','按钮','2',NULL,NULL,1,'admin','2019-03-18 23:25:40','1','2022-02-15 15:00:48'),('86f19c7e0a73a0bae451021ac05b99dd','ac2f7c0c5c5775fcea7e2387bcb22f01','菜单','1',NULL,NULL,1,'admin','2019-03-18 23:25:27','1','2022-02-15 15:00:41'),('948923658baa330319e59b2213cda97c','880a895c98afeca9d9ac39f29e67c13e','添加','2','',2,1,'admin','2019-07-22 10:54:59','admin','2019-07-22 10:55:36'),('a1e7d1ca507cff4a480c8caba7c1339e','880a895c98afeca9d9ac39f29e67c13e','导出','6','',6,1,'admin','2019-07-22 12:06:50',NULL,NULL),('aa0d8a8042a18715a17f0a888d360aa4','ac2f7c0c5c5775fcea7e2387bcb22f01','目录','0',NULL,NULL,1,'admin','2019-03-18 23:24:52','1','2022-02-15 15:00:33'),('b2a8b4bb2c8e66c2c4b1bb086337f393','3486f32803bb953e7155dab3513dc68b','正常','0',NULL,NULL,1,'admin','2022-10-18 21:46:48','admin','2019-03-28 22:22:20'),('bcec04526b04307e24a005d6dcd27fd6','880a895c98afeca9d9ac39f29e67c13e','导入','5','',5,1,'admin','2019-07-22 12:06:41',NULL,NULL),('c53da022b9912e0aed691bbec3c78473','880a895c98afeca9d9ac39f29e67c13e','查询','1','',1,1,'admin','2019-07-22 10:54:51',NULL,NULL),('df168368dcef46cade2aadd80100d8aa','3d9a351be3436fbefb1307d4cfb49bf2','男','1',NULL,1,1,NULL,'2027-08-04 14:56:49','admin','2019-03-23 22:44:44'),('f37f90c496ec9841c4c326b065e00bb2','83bfb33147013cc81640d5fd9eda030c','登录日志','1',NULL,NULL,1,'admin','2019-03-18 23:22:37',NULL,NULL),('f80a8f6838215753b05e1a5ba3346d22','880a895c98afeca9d9ac39f29e67c13e','删除','4','',4,1,'admin','2019-07-22 10:55:14','admin','2019-07-22 10:55:30');
/*!40000 ALTER TABLE `sys_dict_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_log`
--

DROP TABLE IF EXISTS `sys_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_log` (
  `id` varchar(32) NOT NULL,
  `log_type` int DEFAULT '0' COMMENT '日志类型（0登录日志1操作日志）',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) DEFAULT NULL COMMENT '用户操作',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` varchar(5000) DEFAULT NULL COMMENT '请求参数',
  `time` bigint NOT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_log`
--

LOCK TABLES `sys_log` WRITE;
/*!40000 ALTER TABLE `sys_log` DISABLE KEYS */;
INSERT INTO `sys_log` VALUES ('1490997022981996546',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"服务器信息\",\"url\":\"/monitor/systemInfo\",\"component\":\"monitor/SystemInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":1,\"id\":\"1490853450420862977\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 9:02:16 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 6:32:46 PM\",\"delFlag\":0}]',27,'127.0.0.1','2022-02-08 18:32:46'),('1490997090589982721',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"Jvm信息\",\"url\":\"/monitor/jvmInfo\",\"component\":\"monitor/JvmInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":2,\"id\":\"1490921563409633282\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 1:32:55 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 6:33:02 PM\",\"delFlag\":0}]',14,'127.0.0.1','2022-02-08 18:33:02'),('1490997115604811777',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"Tomcat信息\",\"url\":\"/monitor/tomcatInfo\",\"component\":\"monitor/TomcatInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":3,\"id\":\"1490932823836610561\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 2:17:40 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 6:33:08 PM\",\"delFlag\":0}]',20,'127.0.0.1','2022-02-08 18:33:08'),('1491007833834016769',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1486979057737142274\",\"name\":\"HttpTrace追踪\",\"url\":\"/monitor/httpTrace\",\"component\":\"monitor/TttpTrace.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":4,\"id\":\"1491007833729159169\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 7:15:43 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 7:15:43 PM\",\"delFlag\":0}]',18,'127.0.0.1','2022-02-08 19:15:44'),('1491007877714825218',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"请求追踪\",\"url\":\"/monitor/httpTrace\",\"component\":\"monitor/TttpTrace.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":4,\"id\":\"1491007833729159169\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 7:15:44 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 7:15:54 PM\",\"delFlag\":0}]',12,'127.0.0.1','2022-02-08 19:15:54'),('1491008009717960705',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"请求追踪\",\"url\":\"/monitor/httpTrace\",\"component\":\"monitor/HttpTrace.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":4,\"id\":\"1491007833729159169\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 7:15:44 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 8, 2022, 7:16:25 PM\",\"delFlag\":0}]',6,'127.0.0.1','2022-02-08 19:16:26'),('1491245732173303810',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1475702165283561473\",\"name\":\"日志管理\",\"url\":\"/system/log\",\"component\":\"system/LogList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":5,\"id\":\"1491245732043280385\",\"creator\":\"admin\",\"ctime\":\"Feb 9, 2022, 11:01:03 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 9, 2022, 11:01:03 AM\",\"delFlag\":0}]',21,'127.0.0.1','2022-02-09 11:01:03'),('1491397070773346305',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1232913193820581889\"]',11,'127.0.0.1','2022-02-09 21:02:25'),('1491397120920444929',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1356445645198135298\"]',21,'127.0.0.1','2022-02-09 21:02:37'),('1491397140352655361',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1199517671259906049\"]',19,'127.0.0.1','2022-02-09 21:02:42'),('1491397219494977537',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"6b78e3f59faec1a4750acff08030a79b\"]',10,'127.0.0.1','2022-02-09 21:03:01'),('1491699116743364609',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"/system/ScheduleList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":6,\"id\":\"1491699116638507010\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 5:02:38 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 5:02:38 PM\",\"delFlag\":0}]',17,'127.0.0.1','2022-02-10 17:02:39'),('1491699149807063041',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"/system/ScheduleList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":5,\"id\":\"1491699116638507010\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 5:02:38 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 5:02:46 PM\",\"delFlag\":0}]',10,'127.0.0.1','2022-02-10 17:02:46'),('1491699182254198785',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"system/ScheduleList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"\",\"orderNum\":5,\"id\":\"1491699116638507010\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 5:02:38 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 5:02:54 PM\",\"delFlag\":0}]',7,'127.0.0.1','2022-02-10 17:02:54'),('1491732372704497665',1,'admin','保存定时任务','com.athena.modules.job.controller.ScheduleJobController.save()','[{\"beanName\":\"testTask\",\"parameter\":\"athena\",\"cronExpression\":\"0 0/30 * * * ?\",\"status\":0,\"remark\":\"有参测试，多个参数使用json\",\"id\":\"1491732372486393857\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 7:14:47 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 7:14:47 PM\",\"delFlag\":0}]',196,'127.0.0.1','2022-02-10 19:14:47'),('1491735370826563586',1,'admin','删除定时任务','com.athena.modules.job.controller.ScheduleJobController.delete()','[\"1491732372486393857\"]',41,'127.0.0.1','2022-02-10 19:26:42'),('1491736261369577473',1,'admin','保存定时任务','com.athena.modules.job.controller.ScheduleJobController.save()','[{\"beanName\":\"testTask\",\"parameter\":\"athena\",\"cronExpression\":\"0 0/30 * * * ?\",\"status\":0,\"remark\":\"有参测试，多个参数使用json\",\"id\":\"1491736261201805313\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 7:30:14 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 7:30:14 PM\",\"delFlag\":0}]',93,'127.0.0.1','2022-02-10 19:30:14'),('1491741637599064066',1,'admin','立即执行任务','com.athena.modules.job.controller.ScheduleJobController.run()','[\"1491736261201805313\"]',39,'127.0.0.1','2022-02-10 19:51:36'),('1491741983541059585',1,'admin','立即执行任务','com.athena.modules.job.controller.ScheduleJobController.run()','[\"1491736261201805313\"]',35,'127.0.0.1','2022-02-10 19:52:59'),('1491742461427474433',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',34,'127.0.0.1','2022-02-10 19:54:53'),('1491742700469248002',1,'admin','恢复定时任务','com.athena.modules.job.controller.ScheduleJobController.resume()','[\"1491736261201805313\"]',19,'127.0.0.1','2022-02-10 19:55:50'),('1491756063656108033',1,'admin','保存角色','com.athena.modules.sys.controller.SysRoleController.save()','[{\"roleName\":\"测试角色\",\"roleCode\":\"aaaa\",\"remark\":\"\",\"id\":\"1491756063547056130\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 8:48:55 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 10, 2022, 8:48:55 PM\",\"delFlag\":0}]',79,'127.0.0.1','2022-02-10 20:48:56'),('1491758242332794882',1,'admin','删除角色','com.athena.modules.sys.controller.SysRoleController.delete()','[\"1491756063547056130\"]',39,'127.0.0.1','2022-02-10 20:57:35'),('1491956856866742274',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"0\",\"name\":\"首页\",\"url\":\"/dashboard/analysis\",\"component\":\"dashboard/index.tsx\",\"type\":0,\"icon\":\"AppstoreOutlined\",\"orderNum\":0,\"id\":\"1475678576111116290\",\"ctime\":\"Dec 28, 2021, 12:02:44 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 11, 2022, 10:06:48 AM\",\"delFlag\":0}]',12,'127.0.0.1','2022-02-11 10:06:49'),('1493021434140700673',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"用户管理\",\"url\":\"/system/user\",\"component\":\"system/UserList.tsx\",\"perms\":\"sys:user:list\",\"type\":1,\"orderNum\":0,\"id\":\"1475705421565108225\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 8:37:03 AM\",\"delFlag\":0}]',11,'127.0.0.1','2022-02-14 08:37:04'),('1493036550387208194',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"用户管理\",\"url\":\"/system/user\",\"component\":\"system/UserList.tsx\",\"perms\":\"\",\"type\":1,\"orderNum\":0,\"id\":\"1475705421565108225\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:37:07 AM\",\"delFlag\":0}]',14,'127.0.0.1','2022-02-14 09:37:08'),('1493036615528943617',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"用户管理\",\"url\":\"/system/user\",\"component\":\"system/UserList.tsx\",\"perms\":\"sys:user:list\",\"type\":1,\"orderNum\":0,\"id\":\"1475705421565108225\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:37:23 AM\",\"delFlag\":0}]',19,'127.0.0.1','2022-02-14 09:37:23'),('1493036989916712961',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1475705421565108225\",\"name\":\"用户新增\",\"url\":\"\",\"component\":\"\",\"perms\":\"sys:user:save\",\"type\":2,\"icon\":\"\",\"orderNum\":0,\"id\":\"1493036989883158530\",\"creator\":\"admin\",\"ctime\":\"Feb 14, 2022, 9:38:52 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:38:52 AM\",\"delFlag\":0}]',10,'127.0.0.1','2022-02-14 09:38:52'),('1493037125266903041',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1475705421565108225\",\"name\":\"用户修改\",\"url\":\"\",\"component\":\"\",\"perms\":\"sys:user:update\",\"type\":2,\"icon\":\"\",\"orderNum\":1,\"id\":\"1493037125191405569\",\"creator\":\"admin\",\"ctime\":\"Feb 14, 2022, 9:39:24 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:39:24 AM\",\"delFlag\":0}]',21,'127.0.0.1','2022-02-14 09:39:25'),('1493037227964436482',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1475705421565108225\",\"name\":\"用户删除\",\"url\":\"\",\"component\":\"\",\"perms\":\"sys:user:delete\",\"type\":2,\"icon\":\"\",\"orderNum\":2,\"id\":\"1493037227893133313\",\"creator\":\"admin\",\"ctime\":\"Feb 14, 2022, 9:39:49 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:39:49 AM\",\"delFlag\":0}]',18,'127.0.0.1','2022-02-14 09:39:49'),('1493037339197378561',1,'admin','保存菜单','com.athena.modules.sys.controller.SysPermissionController.save()','[{\"parentId\":\"1475705421565108225\",\"name\":\"用户批量删除\",\"url\":\"\",\"component\":\"\",\"perms\":\"sys:user:deleteBatch\",\"type\":2,\"icon\":\"\",\"orderNum\":3,\"id\":\"1493037339163824129\",\"creator\":\"admin\",\"ctime\":\"Feb 14, 2022, 9:40:15 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 14, 2022, 9:40:15 AM\",\"delFlag\":0}]',8,'127.0.0.1','2022-02-14 09:40:16'),('1493143671825092609',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',28,'127.0.0.1','2022-02-14 16:42:47'),('1493456444756963329',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"0\",\"name\":\"系统管理\",\"url\":\"/system\",\"component\":\"layout/RouterView.tsx\",\"type\":0,\"icon\":\"SettingOutlined\",\"orderNum\":0,\"id\":\"1475702165283561473\",\"ctime\":\"Dec 28, 2021, 1:36:28 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 1:25:38 PM\",\"delFlag\":0}]',44,'127.0.0.1','2022-02-15 13:25:38'),('1493456675120721921',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"用户管理\",\"url\":\"/system/user\",\"component\":\"system/UserList.tsx\",\"perms\":\"sys:user:list\",\"type\":1,\"icon\":\"UserOutlined\",\"orderNum\":0,\"id\":\"1475705421565108225\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:26:33 PM\",\"delFlag\":0}]',14,'127.0.0.1','2022-02-15 13:26:33'),('1493457212855660546',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"角色管理\",\"url\":\"/system/role\",\"component\":\"system/RoleList.tsx\",\"type\":1,\"icon\":\"TeamOutlined\",\"orderNum\":1,\"id\":\"1475705421628022785\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 1:28:41 PM\",\"delFlag\":0}]',8,'127.0.0.1','2022-02-15 13:28:41'),('1493457683628535810',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"菜单管理\",\"url\":\"/system/permission\",\"component\":\"system/PermissionList.tsx\",\"type\":1,\"icon\":\"PartitionOutlined\",\"orderNum\":2,\"id\":\"1475705421636411394\",\"ctime\":\"Dec 28, 2021, 1:49:24 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 1:30:33 PM\",\"delFlag\":0}]',8,'127.0.0.1','2022-02-15 13:30:34'),('1493457773780905986',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"部门管理\",\"url\":\"/system/dept\",\"component\":\"system/DeptList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"ApartmentOutlined\",\"orderNum\":4,\"id\":\"1485420753806172161\",\"creator\":\"admin\",\"ctime\":\"Jan 24, 2022, 9:14:40 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:30:55 PM\",\"delFlag\":0}]',16,'127.0.0.1','2022-02-15 13:30:55'),('1493458119857123330',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"字典管理\",\"url\":\"/system/dict\",\"component\":\"system/DictList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"SortAscendingOutlined\",\"orderNum\":3,\"id\":\"1483688293013995521\",\"creator\":\"admin\",\"ctime\":\"Jan 19, 2022, 2:30:29 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:32:17 PM\",\"delFlag\":0}]',10,'127.0.0.1','2022-02-15 13:32:18'),('1493458374828863490',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1475702165283561473\",\"name\":\"日志管理\",\"url\":\"/system/log\",\"component\":\"system/LogList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"FileSearchOutlined\",\"orderNum\":5,\"id\":\"1491245732043280385\",\"creator\":\"admin\",\"ctime\":\"Feb 9, 2022, 11:01:03 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:33:18 PM\",\"delFlag\":0}]',18,'127.0.0.1','2022-02-15 13:33:18'),('1493458736608555009',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"0\",\"name\":\"首页\",\"url\":\"/dashboard/analysis\",\"component\":\"dashboard/index.tsx\",\"type\":0,\"icon\":\"HomeOutlined\",\"orderNum\":0,\"id\":\"1475678576111116290\",\"ctime\":\"Dec 28, 2021, 12:02:44 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:34:44 PM\",\"delFlag\":0}]',5,'127.0.0.1','2022-02-15 13:34:45'),('1493458844158898177',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"0\",\"name\":\"系统监控\",\"url\":\"/monitor\",\"component\":\"layout/RouterView.tsx\",\"perms\":\"\",\"type\":0,\"icon\":\"BarChartOutlined\",\"orderNum\":1,\"id\":\"1486979057737142274\",\"creator\":\"admin\",\"ctime\":\"Jan 28, 2022, 4:26:49 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:35:10 PM\",\"delFlag\":0}]',17,'127.0.0.1','2022-02-15 13:35:10'),('1493459521044066306',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"SQL监控\",\"url\":\"/monitor/druid\",\"component\":\"layout/InnerLink.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"ConsoleSqlOutlined\",\"orderNum\":0,\"id\":\"1486983766581579778\",\"creator\":\"admin\",\"ctime\":\"Jan 28, 2022, 4:45:31 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:37:51 PM\",\"delFlag\":0}]',17,'127.0.0.1','2022-02-15 13:37:52'),('1493459614405079041',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"服务器信息\",\"url\":\"/monitor/systemInfo\",\"component\":\"monitor/SystemInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"DatabaseOutlined\",\"orderNum\":1,\"id\":\"1490853450420862977\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 9:02:16 AM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:38:13 PM\",\"delFlag\":0}]',17,'127.0.0.1','2022-02-15 13:38:14'),('1493460070518861826',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"Jvm信息\",\"url\":\"/monitor/jvmInfo\",\"component\":\"monitor/JvmInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"RobotOutlined\",\"orderNum\":2,\"id\":\"1490921563409633282\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 1:32:55 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:40:02 PM\",\"delFlag\":0}]',20,'127.0.0.1','2022-02-15 13:40:03'),('1493460125602656257',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"system/ScheduleList.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"ClockCircleOutlined\",\"orderNum\":5,\"id\":\"1491699116638507010\",\"creator\":\"admin\",\"ctime\":\"Feb 10, 2022, 5:02:38 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:40:15 PM\",\"delFlag\":0}]',8,'127.0.0.1','2022-02-15 13:40:16'),('1493460291722260481',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"请求追踪\",\"url\":\"/monitor/httpTrace\",\"component\":\"monitor/HttpTrace.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"CreditCardOutlined\",\"orderNum\":4,\"id\":\"1491007833729159169\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 7:15:44 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:40:55 PM\",\"delFlag\":0}]',6,'127.0.0.1','2022-02-15 13:40:55'),('1493460380247240706',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"Tomcat信息\",\"url\":\"/monitor/tomcatInfo\",\"component\":\"monitor/TomcatInfo.tsx\",\"perms\":\"\",\"type\":1,\"icon\":\"CoffeeOutlined\",\"orderNum\":3,\"id\":\"1490932823836610561\",\"creator\":\"admin\",\"ctime\":\"Feb 8, 2022, 2:17:40 PM\",\"editor\":\"admin\",\"mtime\":\"Feb 15, 2022, 1:41:16 PM\",\"delFlag\":0}]',18,'127.0.0.1','2022-02-15 13:41:16'),('1493479403282345985',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"0b5d19e1fce4b2e6647e6b4a17760c14\"]',30,'127.0.0.1','2022-02-15 14:56:52'),('1493479478507188225',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1199518099888414722\"]',13,'127.0.0.1','2022-02-15 14:57:10'),('1493479490561617922',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1199520177767587841\"]',10,'127.0.0.1','2022-02-15 14:57:13'),('1493479508253192194',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1199525215290306561\"]',9,'127.0.0.1','2022-02-15 14:57:17'),('1493479541027483650',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1209733563293962241\"]',10,'127.0.0.1','2022-02-15 14:57:25'),('1493479560396779521',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1280401766745718786\"]',8,'127.0.0.1','2022-02-15 14:57:29'),('1493479573369761794',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1435806345473363969\"]',8,'127.0.0.1','2022-02-15 14:57:32'),('1493479580978229249',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1435811260815970306\"]',8,'127.0.0.1','2022-02-15 14:57:34'),('1493479588662194178',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1436342704279470082\"]',8,'127.0.0.1','2022-02-15 14:57:36'),('1493479596274855937',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1437574114703335425\"]',8,'127.0.0.1','2022-02-15 14:57:38'),('1493479603019296770',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1438405902849454081\"]',9,'127.0.0.1','2022-02-15 14:57:40'),('1493479610963308546',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1447766666284081154\"]',7,'127.0.0.1','2022-02-15 14:57:41'),('1493479619016372225',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1448849905271668738\"]',7,'127.0.0.1','2022-02-15 14:57:43'),('1493479626100551681',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1451431472158449666\"]',19,'127.0.0.1','2022-02-15 14:57:45'),('1493479634182975489',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1452904580253999105\"]',19,'127.0.0.1','2022-02-15 14:57:47'),('1493479641120354306',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1462993456402243585\"]',19,'127.0.0.1','2022-02-15 14:57:49'),('1493479646942048257',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"236e8a4baff0db8c62c00dd95632834f\"]',7,'127.0.0.1','2022-02-15 14:57:50'),('1493479663908007938',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"2e02df51611a4b9632828ab7e5338f00\"]',20,'127.0.0.1','2022-02-15 14:57:54'),('1493479672720240641',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"2f0320997ade5dd147c90130f7218c3e\"]',9,'127.0.0.1','2022-02-15 14:57:56'),('1493479701971316738',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4274efc2292239b6f000b153f50823ff\"]',20,'127.0.0.1','2022-02-15 14:58:03'),('1493479709730779138',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4c03fca6bf1f0299c381213961566349\"]',19,'127.0.0.1','2022-02-15 14:58:05'),('1493479721634213889',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4c753b5293304e7a445fd2741b46529d\"]',16,'127.0.0.1','2022-02-15 14:58:08'),('1493479730618413057',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4d7fec1a7799a436d26d02325eff295e\"]',21,'127.0.0.1','2022-02-15 14:58:10'),('1493479738495315969',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4e4602b3e3686f0911384e188dc7efb4\"]',16,'127.0.0.1','2022-02-15 14:58:12'),('1493479745747267586',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"4f69be5f507accea8d5df5f11346181a\"]',9,'127.0.0.1','2022-02-15 14:58:14'),('1493479754660163586',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"68168534ff5065a152bfab275c2136f8\"]',8,'127.0.0.1','2022-02-15 14:58:16'),('1493479762042138625',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"72cce0989df68887546746d8f09811aa\"]',9,'127.0.0.1','2022-02-15 14:58:17'),('1493479770950840321',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"78bda155fe380b1b3f175f1e88c284c6\"]',8,'127.0.0.1','2022-02-15 14:58:20'),('1493479834817507329',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"8dfe32e2d29ea9430a988b3b558bf233\"]',9,'127.0.0.1','2022-02-15 14:58:35'),('1493479847907930114',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"a7adbcd86c37f7dbc9b66945c82ef9e6\"]',44,'127.0.0.1','2022-02-15 14:58:38'),('1493479854899834881',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"a9d9942bd0eccb6e89de92d130ec4c4a\"]',8,'127.0.0.1','2022-02-15 14:58:40'),('1493479901586632705',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"ad7c65ba97c20a6805d5dcdf13cdaf36\"]',8,'127.0.0.1','2022-02-15 14:58:51'),('1493479930820931585',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"fc6cd58fde2e8481db10d3a1e68ce70c\"]',9,'127.0.0.1','2022-02-15 14:58:58'),('1493479946763481089',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"d6e1152968b02d69ff358c75b48a6ee1\"]',17,'127.0.0.1','2022-02-15 14:59:01'),('1493479960126533633',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"c5a14c75172783d72cbee6ee7f5df5d1\"]',7,'127.0.0.1','2022-02-15 14:59:05'),('1493479977906188290',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"c36169beb12de8a71c8683ee7c28a503\"]',8,'127.0.0.1','2022-02-15 14:59:09'),('1493479998919651329',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"bd1b8bc28e65d6feefefb6f3c79f42fd\"]',6,'127.0.0.1','2022-02-15 14:59:14'),('1493480072022175746',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1178295274528845826\"]',8,'127.0.0.1','2022-02-15 14:59:31'),('1493480102577680386',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"845da5006c97754728bf48b6a10f79cc\"]',12,'127.0.0.1','2022-02-15 14:59:39'),('1493480332626866178',1,'admin','修改字典','com.athena.modules.sys.controller.SysDictItemController.update()','[{\"dictId\":\"ac2f7c0c5c5775fcea7e2387bcb22f01\",\"itemText\":\"目录\",\"itemValue\":\"0\",\"id\":\"aa0d8a8042a18715a17f0a888d360aa4\",\"creator\":\"admin\",\"ctime\":\"Mar 18, 2019, 11:24:52 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 3:00:33 PM\",\"delFlag\":1}]',211,'127.0.0.1','2022-02-15 15:00:33'),('1493480363060736002',1,'admin','修改字典','com.athena.modules.sys.controller.SysDictItemController.update()','[{\"dictId\":\"ac2f7c0c5c5775fcea7e2387bcb22f01\",\"itemText\":\"菜单\",\"itemValue\":\"1\",\"id\":\"86f19c7e0a73a0bae451021ac05b99dd\",\"creator\":\"admin\",\"ctime\":\"Mar 18, 2019, 11:25:27 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 3:00:40 PM\",\"delFlag\":1}]',7,'127.0.0.1','2022-02-15 15:00:41'),('1493480394589319170',1,'admin','修改字典','com.athena.modules.sys.controller.SysDictItemController.update()','[{\"dictId\":\"ac2f7c0c5c5775fcea7e2387bcb22f01\",\"itemText\":\"按钮\",\"itemValue\":\"2\",\"id\":\"7ccf7b80c70ee002eceb3116854b75cb\",\"creator\":\"admin\",\"ctime\":\"Mar 18, 2019, 11:25:40 PM\",\"editor\":\"1\",\"mtime\":\"Feb 15, 2022, 3:00:48 PM\",\"delFlag\":1}]',5,'127.0.0.1','2022-02-15 15:00:48'),('1493740259095777281',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"1174511106530525185\"]',10,'127.0.0.1','2022-02-16 08:13:25'),('2084834340834164737',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"monitor/quartz\",\"perms\":\"\",\"type\":1,\"icon\":\"ClockCircleOutlined\",\"orderNum\":5,\"id\":\"1491699116638507010\",\"editor\":\"admin\",\"mtime\":\"Aug 5, 2026, 10:50:15 AM\"}]',386,'[0:0:0:0:0:0:0:1]','2026-08-05 10:50:16'),('2087517091384655874',1,'admin','立即执行任务','com.athena.modules.job.controller.ScheduleJobController.run()','[\"1491736261201805313\"]',96,'[0:0:0:0:0:0:0:1]','2026-08-12 20:30:33'),('2087517170778636289',1,'admin','立即执行任务','com.athena.modules.job.controller.ScheduleJobController.run()','[\"1491736261201805313\"]',13,'[0:0:0:0:0:0:0:1]','2026-08-12 20:30:52'),('2087517731888431105',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',19,'[0:0:0:0:0:0:0:1]','2026-08-12 20:33:06'),('2087517742944616449',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',7,'[0:0:0:0:0:0:0:1]','2026-08-12 20:33:09'),('2087517769616195585',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',7,'[0:0:0:0:0:0:0:1]','2026-08-12 20:33:15'),('2087522225778982913',1,'admin','保存定时任务','com.athena.modules.job.controller.ScheduleJobController.save()','[{\"beanName\":\"1111\",\"parameter\":\"1\",\"cronExpression\":\"0 0/30 * * * ?\",\"status\":0,\"remark\":\"\",\"id\":\"2087522224373891073\",\"creator\":\"admin\",\"ctime\":\"Aug 12, 2026, 8:50:57 PM\",\"editor\":\"admin\",\"mtime\":\"Aug 12, 2026, 8:50:57 PM\",\"delFlag\":0}]',645,'[0:0:0:0:0:0:0:1]','2026-08-12 20:50:58'),('2087522318309523457',1,'admin','立即执行任务','com.athena.modules.job.controller.ScheduleJobController.run()','[\"2087522224373891073\"]',32,'[0:0:0:0:0:0:0:1]','2026-08-12 20:51:20'),('2087523552454115330',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',8,'[0:0:0:0:0:0:0:1]','2026-08-12 20:56:14'),('2087524190151901186',1,'admin','恢复定时任务','com.athena.modules.job.controller.ScheduleJobController.resume()','[\"1491736261201805313\"]',22,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:46'),('2087524197164777473',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',11,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:48'),('2087524213317042177',1,'admin','恢复定时任务','com.athena.modules.job.controller.ScheduleJobController.resume()','[\"1491736261201805313\"]',15,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:51'),('2087524218429898754',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"1491736261201805313\"]',12,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:53'),('2087524231973306370',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"2087522224373891073\"]',13,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:56'),('2087524236113084417',1,'admin','恢复定时任务','com.athena.modules.job.controller.ScheduleJobController.resume()','[\"2087522224373891073\"]',11,'[0:0:0:0:0:0:0:1]','2026-08-12 20:58:57'),('2087692607861424129',1,'admin','暂停定时任务','com.athena.modules.job.controller.ScheduleJobController.pause()','[\"2087522224373891073\"]',32,'[0:0:0:0:0:0:0:1]','2026-08-13 08:08:00'),('2087710712037789698',1,'admin','删除定时任务','com.athena.modules.job.controller.ScheduleJobController.delete()','[\"2087522224373891073\"]',54,'[0:0:0:0:0:0:0:1]','2026-08-13 09:19:56'),('2087716404027084801',1,'admin','保存用户','com.athena.modules.sys.controller.SysUserController.save()','[{\"username\":\"111111111\",\"realname\":\"hahaaha\",\"password\":\"$2a$10$E2QjmxiCBly/GxevuA/vCejdQnSb3LXApHTCeo/hrdeKGxEKp93pO\",\"email\":\"11111@163.com\",\"mobile\":\"111111\",\"sex\":1,\"deptCode\":\"1485429924664459265\",\"roleIdList\":[\"1485842742563291137\"],\"id\":\"2087716403699929089\",\"creator\":\"admin\",\"ctime\":\"Aug 13, 2026, 9:42:33 AM\",\"editor\":\"admin\",\"mtime\":\"Aug 13, 2026, 9:42:33 AM\",\"delFlag\":0}]',244,'[0:0:0:0:0:0:0:1]','2026-08-13 09:42:33'),('2087716424344293377',1,'admin','删除用户','com.athena.modules.sys.controller.SysUserController.delete()','[\"2087716403699929089\"]',12,'[0:0:0:0:0:0:0:1]','2026-08-13 09:42:38'),('2087716522264514562',1,'admin','保存用户','com.athena.modules.sys.controller.SysUserController.save()','[{\"username\":\"111111\",\"realname\":\"11111\",\"password\":\"$2a$10$OlYinlAGFo1Hod1CQzisCORp8G/0NddqQQVx6ZhEGlXUCJoJ6dpI2\",\"email\":\"1111@163.com\",\"mobile\":\"1111111\",\"sex\":1,\"deptCode\":\"1485429843877969921\",\"roleIdList\":[\"1485842742563291137\"],\"id\":\"2087716522184822785\",\"creator\":\"admin\",\"ctime\":\"Aug 13, 2026, 9:43:01 AM\",\"editor\":\"admin\",\"mtime\":\"Aug 13, 2026, 9:43:01 AM\",\"delFlag\":0}]',99,'[0:0:0:0:0:0:0:1]','2026-08-13 09:43:01'),('2087716538974621698',1,'admin','删除用户','com.athena.modules.sys.controller.SysUserController.delete()','[\"2087716522184822785\"]',13,'[0:0:0:0:0:0:0:1]','2026-08-13 09:43:05'),('2087718400624840705',1,'admin','保存用户','com.athena.modules.sys.controller.SysUserController.save()','[{\"username\":\"1111\",\"realname\":\"1111\",\"password\":\"$2a$10$dFMxOmR9ANcBelhugyhyU.0Pc0DEV2u.Delz2YAfj/8KRj0QJmtLa\",\"email\":\"111@163.com\",\"mobile\":\"111111\",\"sex\":1,\"deptCode\":\"1485429924664459265\",\"roleIdList\":[\"1485842742563291137\"],\"id\":\"2087718400566120449\",\"creator\":\"admin\",\"ctime\":\"Aug 13, 2026, 9:50:29 AM\",\"editor\":\"admin\",\"mtime\":\"Aug 13, 2026, 9:50:29 AM\",\"delFlag\":0}]',96,'[0:0:0:0:0:0:0:1]','2026-08-13 09:50:29'),('2087718413153226753',1,'admin','删除用户','com.athena.modules.sys.controller.SysUserController.delete()','[\"2087718400566120449\"]',12,'[0:0:0:0:0:0:0:1]','2026-08-13 09:50:32'),('2110347517597360130',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"服务器信息\",\"url\":\"/monitor/systemInfo\",\"component\":\"monitor/SystemInfo\",\"frameSrc\":\"\",\"redirect\":\"\",\"perms\":\"\",\"type\":1,\"icon\":\"server\",\"orderNum\":2,\"id\":\"1490853450420862977\",\"editor\":\"admin\",\"mtime\":\"Oct 14, 2026, 8:30:31 PM\"}]',31,'[0:0:0:0:0:0:0:1]','2026-10-14 20:30:31'),('2110347544193441793',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"SQL监控\",\"url\":\"/monitor/druid\",\"component\":\"iframe\",\"frameSrc\":\"http://localhost:8080/athena/druid\",\"frameBlank\":1,\"redirect\":\"\",\"perms\":\"\",\"type\":1,\"icon\":\"data-base\",\"orderNum\":1,\"id\":\"1486983766581579778\",\"editor\":\"admin\",\"mtime\":\"Oct 14, 2026, 8:30:37 PM\"}]',12,'[0:0:0:0:0:0:0:1]','2026-10-14 20:30:38'),('2110347592809619458',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"SQL监控\",\"url\":\"/monitor/druid\",\"component\":\"iframe\",\"frameSrc\":\"http://localhost:8080/athena/druid\",\"frameBlank\":1,\"redirect\":\"\",\"perms\":\"\",\"type\":1,\"icon\":\"data-base\",\"orderNum\":3,\"id\":\"1486983766581579778\",\"editor\":\"admin\",\"mtime\":\"Oct 14, 2026, 8:30:49 PM\"}]',10,'[0:0:0:0:0:0:0:1]','2026-10-14 20:30:49'),('2110347619653165057',1,'admin','修改菜单','com.athena.modules.sys.controller.SysPermissionController.update()','[{\"parentId\":\"1486979057737142274\",\"name\":\"定时任务\",\"url\":\"/monitor/schedule\",\"component\":\"monitor/quartz\",\"frameSrc\":\"\",\"redirect\":\"\",\"perms\":\"\",\"type\":1,\"icon\":\"lock-time\",\"orderNum\":1,\"id\":\"1491699116638507010\",\"editor\":\"admin\",\"mtime\":\"Oct 14, 2026, 8:30:55 PM\"}]',11,'[0:0:0:0:0:0:0:1]','2026-10-14 20:30:56'),('2110687923237355522',1,'admin','重置密码','com.athena.modules.sys.controller.SysUserController.resetPassword()','[{\"id\":\"1\",\"password\":\"dummy\"}]',3,'[0:0:0:0:0:0:0:1]','2026-10-15 19:03:10'),('2111990307201798145',1,'zz_pt_escalate','添加用户到角色','com.athena.modules.sys.controller.SysRoleController.addUser()','[{\"roleId\":\"1485529471914319874\",\"userIdList\":[\"2111990124963483649\"]}]',8,'[0:0:0:0:0:0:0:1]','2026-10-19 09:18:23'),('2111990494896902146',1,'zz_pt_user1','修改密码','com.athena.modules.sys.controller.SysUserController.password()','[{\"password\":\"8a4503dc3352dc74f693d4782cfa4e7741d4fd6b398403dae8564868630e4b00b91ae945b93348b08c3f2f5c42e9991d192a180e1fb84e3884957bf3f963cf009692da179e3f332651b7240d833696d2dabddc31065740aebfc5c1e1bcefb2a48ee339f231880a0b8a695c4ef323bd5e2bd839ae30fbc6f1b141f8f1fbe8a9c5\",\"newPassword\":\"b6efd3c628457d8948df42b733c6b3c5791cd465dedef3eb50eaf7e1b0e6299881d8e4d8ebab7c273594d117b1cec7672a1bcad515e11af6c7f093627dfce0c2d12715e83922b28be317dc744f21790cc0e4db94b1fb6e72b6dccdd367806a0a5709bb6ee55c91aa5de2305074c1b23e45ad4b8520dcbfe9d6896047c4d237ea\"}]',226,'[0:0:0:0:0:0:0:1]','2026-10-19 09:19:08'),('2113109823256391682',1,'admin','修改角色','com.athena.modules.sys.controller.SysRoleController.update()','[{\"roleName\":\"Administrator\",\"roleCode\":\"admin\",\"remark\":\"updated by admin\",\"id\":\"1485529471914319874\",\"editor\":\"admin\",\"mtime\":\"Oct 22, 2026, 11:26:56 AM\"}]',18,'[0:0:0:0:0:0:0:1]','2026-10-22 11:26:56'),('2113109832559357953',1,'admin','修改角色','com.athena.modules.sys.controller.SysRoleController.update()','[{\"roleName\":\"Admin\",\"roleCode\":\"admin\",\"remark\":\"\",\"id\":\"1485529471914319874\",\"editor\":\"admin\",\"mtime\":\"Oct 22, 2026, 11:26:58 AM\"}]',11,'[0:0:0:0:0:0:0:1]','2026-10-22 11:26:58'),('2113110664218517505',1,'admin','重置密码','com.athena.modules.sys.controller.SysUserController.resetPassword()','[{\"id\":\"2113110646350782466\",\"password\":\"***\"}]',108,'[0:0:0:0:0:0:0:1]','2026-10-22 11:30:17'),('2114898528373755905',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictController.delete()','[\"2114898512842248193\"]',86,'0:0:0:0:0:0:0:1','2026-10-27 09:54:37'),('2114916370548727810',1,'admin','保存字典','com.athena.modules.sys.controller.SysDictItemController.save()','[{\"dictId\":\"3d9a351be3436fbefb1307d4cfb49bf2\",\"itemText\":\"其他\",\"itemValue\":\"3\",\"sortOrder\":3,\"id\":\"2114916370301263874\",\"creator\":\"admin\",\"ctime\":\"Oct 27, 2026, 11:05:30 AM\",\"editor\":\"admin\",\"mtime\":\"Oct 27, 2026, 11:05:30 AM\",\"delFlag\":0}]',167,'127.0.0.1','2026-10-27 11:05:31'),('2114922190845976577',1,'admin','保存字典','com.athena.modules.sys.controller.SysDictItemController.save()','[{\"dictId\":\"3d9a351be3436fbefb1307d4cfb49bf2\",\"itemText\":\"测试\",\"itemValue\":\"12\",\"sortOrder\":4,\"id\":\"2114922190745313282\",\"creator\":\"admin\",\"ctime\":\"Oct 27, 2026, 11:28:38 AM\",\"editor\":\"admin\",\"mtime\":\"Oct 27, 2026, 11:28:38 AM\",\"delFlag\":0}]',31,'127.0.0.1','2026-10-27 11:28:38'),('2114922247943036929',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictItemController.delete()','[\"2114922190745313282\"]',28,'127.0.0.1','2026-10-27 11:28:52'),('2114923344602505217',1,'admin','保存字典','com.athena.modules.sys.controller.SysDictItemController.save()','[{\"dictId\":\"2114923322104258562\",\"itemText\":\"Charlie\",\"itemValue\":\"Charlie_v\",\"sortOrder\":2,\"id\":\"2114923344539590657\",\"creator\":\"admin\",\"ctime\":\"Oct 27, 2026, 11:33:13 AM\",\"editor\":\"admin\",\"mtime\":\"Oct 27, 2026, 11:33:13 AM\",\"delFlag\":0}]',17,'0:0:0:0:0:0:0:1','2026-10-27 11:33:13'),('2114966772392583170',1,'admin','删除字典','com.athena.modules.sys.controller.SysDictItemController.delete()','[\"2114916370301263874\"]',31,'127.0.0.1','2026-10-27 14:25:47'),('2114966825802850305',1,'admin','保存字典','com.athena.modules.sys.controller.SysDictItemController.save()','[{\"dictId\":\"3d9a351be3436fbefb1307d4cfb49bf2\",\"itemText\":\"其他\",\"itemValue\":\"3\",\"sortOrder\":3,\"id\":\"2114966825630883842\",\"creator\":\"admin\",\"ctime\":\"Oct 27, 2026, 2:26:00 PM\",\"editor\":\"admin\",\"mtime\":\"Oct 27, 2026, 2:26:00 PM\",\"delFlag\":0}]',159,'127.0.0.1','2026-10-27 14:26:00'),('2115342032979845122',1,'admin','从角色移除用户','com.athena.modules.sys.controller.SysRoleController.removeUser()','[\"1485842742563291137\",\"2115257030753603585\"]',31,'0:0:0:0:0:0:0:1','2026-10-28 15:16:56'),('2115343592514347010',1,'admin','删除用户','com.athena.modules.sys.controller.SysUserController.delete()','[\"2115257030753603585\"]',17,'0:0:0:0:0:0:0:1','2026-10-28 15:23:08'),('2115343687762796545',1,'admin','修改用户','com.athena.modules.sys.controller.SysUserController.update()','[{\"username\":\"test1\",\"realname\":\"测试\",\"email\":\"hahaha@qq.com\",\"mobile\":\"1333333333\",\"sex\":1,\"deptCode\":\"1485429924664459265\",\"roleIdList\":[\"1485529471914319874\"],\"id\":\"1491744161173450754\",\"editor\":\"admin\",\"mtime\":\"Oct 28, 2026, 3:23:30 PM\"}]',22,'0:0:0:0:0:0:0:1','2026-10-28 15:23:31'),('2115343765504221186',1,'admin','重置密码','com.athena.modules.sys.controller.SysUserController.resetPassword()','[{\"id\":\"1491744161173450754\",\"password\":\"***\"}]',101,'0:0:0:0:0:0:0:1','2026-10-28 15:23:50');
/*!40000 ALTER TABLE `sys_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` varchar(32) NOT NULL,
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) DEFAULT NULL COMMENT '菜单名称',
  `url` varchar(200) DEFAULT NULL COMMENT '菜单URL',
  `component` varchar(255) DEFAULT NULL COMMENT '路由地址',
  `frame_src` varchar(255) DEFAULT NULL COMMENT '外部链接',
  `frame_blank` tinyint DEFAULT NULL COMMENT '0:外链，1内链',
  `perms` varchar(500) DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `redirect` varchar(255) DEFAULT NULL COMMENT '一级菜单跳转地址',
  `type` int DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `hidden` int DEFAULT '1' COMMENT '隐藏路由(0是1否)',
  `order_num` int DEFAULT '0' COMMENT '排序',
  `creator` varchar(32) DEFAULT NULL,
  `ctime` datetime DEFAULT NULL,
  `editor` varchar(32) DEFAULT NULL,
  `mtime` datetime DEFAULT NULL,
  `del_flag` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES ('1475678576111116290','0','首页','/dashboard','layout',NULL,NULL,NULL,'/dashboard/base',0,'home',1,0,NULL,'2021-12-28 12:02:44','admin','2022-02-15 13:34:45',0),('1475702165283561473','0','系统管理','/system','layout',NULL,NULL,NULL,NULL,0,'setting-1',1,1,NULL,'2021-12-28 13:36:28','1','2022-02-15 13:25:38',0),('1475705421565108225','1475702165283561473','用户管理','/system/user','system/user-list',NULL,NULL,'sys:user:list',NULL,1,'user-list',1,0,NULL,'2021-12-28 13:49:24','admin','2022-02-15 13:26:33',0),('1475705421628022785','1475702165283561473','角色管理','/system/role','system/role-list',NULL,NULL,NULL,NULL,1,'usergroup',1,1,NULL,'2021-12-28 13:49:24','1','2022-02-15 13:28:41',0),('1475705421636411394','1475702165283561473','菜单管理','/system/permission','system/permission-list',NULL,NULL,NULL,NULL,1,'menu-application',1,2,NULL,'2021-12-28 13:49:24','1','2022-02-15 13:30:34',0),('1483688293013995521','1475702165283561473','字典管理','/system/dict','system/dict-list',NULL,NULL,'',NULL,1,'filter-sort',1,3,'admin','2022-01-19 14:30:29','admin','2022-02-15 13:32:18',0),('1485420753806172161','1475702165283561473','部门管理','/system/dept','system/dept-list',NULL,NULL,'',NULL,1,'tree-square-dot',1,4,'admin','2022-01-24 09:14:40','admin','2022-02-15 13:30:55',0),('1486979057737142274','0','系统监控','/monitor','layout',NULL,NULL,'',NULL,0,'precise-monitor',1,2,'admin','2022-01-28 16:26:49','admin','2022-02-15 13:35:10',0),('1486983766581579778','1486979057737142274','SQL监控','/monitor/druid','iframe','http://localhost:8080/athena/druid',1,'','',1,'data-base',1,3,'admin','2022-01-28 16:45:31','admin','2026-10-14 20:30:49',0),('1490853450420862977','1486979057737142274','服务器信息','/monitor/systemInfo','monitor/SystemInfo','',NULL,'','',1,'server',1,2,'admin','2022-02-08 09:02:16','admin','2026-10-14 20:30:31',0),('1491245732043280385','1475702165283561473','日志管理','/system/log','system/log-list',NULL,NULL,'',NULL,1,'system-log',1,5,'admin','2022-02-09 11:01:03','admin','2022-02-15 13:33:18',0),('1491699116638507010','1486979057737142274','定时任务','/monitor/schedule','monitor/quartz','',NULL,'','',1,'lock-time',1,1,'admin','2022-02-10 17:02:38','admin','2026-10-14 20:30:56',0),('1493036989883158530','1475705421565108225','用户新增','','',NULL,NULL,'sys:user:save',NULL,2,'',1,0,'admin','2022-02-14 09:38:52','admin','2022-02-14 09:38:52',0),('1493037125191405569','1475705421565108225','用户修改','','',NULL,NULL,'sys:user:update',NULL,2,'',1,1,'admin','2022-02-14 09:39:25','admin','2022-02-14 09:39:25',0),('1493037227893133313','1475705421565108225','用户删除','','',NULL,NULL,'sys:user:delete',NULL,2,'',1,2,'admin','2022-02-14 09:39:49','admin','2022-02-14 09:39:49',0),('1493037339163824129','1475705421565108225','用户批量删除','','',NULL,NULL,'sys:user:deleteBatch',NULL,2,'',1,3,'admin','2022-02-14 09:40:16','admin','2022-02-14 09:40:16',0),('2053295662509240322','1475678576111116290','概览','/dashboard/analysis','dashboard/base/index',NULL,NULL,NULL,NULL,1,'chart-bar',1,1,NULL,'2026-05-10 10:06:49',NULL,'2026-05-10 10:06:49',0),('2053300086472396802','1475678576111116290','统计报表','/dashboard/report','dashboard/detail/index',NULL,NULL,NULL,NULL,1,'chart-scatter',1,2,NULL,'2026-05-10 10:24:24',NULL,'2026-05-10 10:24:24',0),('97392b12c88c11f18a38286ed489eee1','1485420753806172161','部门删除',NULL,NULL,NULL,NULL,'sys:dept:delete',NULL,2,NULL,1,125,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392e12c88c11f18a38286ed489eee1','1485420753806172161','部门详情',NULL,NULL,NULL,NULL,'sys:dept:info',NULL,2,NULL,1,122,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392e73c88c11f18a38286ed489eee1','1485420753806172161','部门列表',NULL,NULL,NULL,NULL,'sys:dept:list',NULL,2,NULL,1,120,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392eb1c88c11f18a38286ed489eee1','1485420753806172161','部门新增',NULL,NULL,NULL,NULL,'sys:dept:save',NULL,2,NULL,1,123,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392eebc88c11f18a38286ed489eee1','1485420753806172161','部门选择',NULL,NULL,NULL,NULL,'sys:dept:select',NULL,2,NULL,1,121,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392f24c88c11f18a38286ed489eee1','1485420753806172161','部门修改',NULL,NULL,NULL,NULL,'sys:dept:update',NULL,2,NULL,1,124,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392f60c88c11f18a38286ed489eee1','1483688293013995521','字典删除',NULL,NULL,NULL,NULL,'sys:dict:delete',NULL,2,NULL,1,134,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392f9ac88c11f18a38286ed489eee1','1483688293013995521','字典详情',NULL,NULL,NULL,NULL,'sys:dict:info',NULL,2,NULL,1,131,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97392fd4c88c11f18a38286ed489eee1','1483688293013995521','字典列表',NULL,NULL,NULL,NULL,'sys:dict:list',NULL,2,NULL,1,130,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739300dc88c11f18a38286ed489eee1','1483688293013995521','字典新增',NULL,NULL,NULL,NULL,'sys:dict:save',NULL,2,NULL,1,132,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393044c88c11f18a38286ed489eee1','1483688293013995521','字典修改',NULL,NULL,NULL,NULL,'sys:dict:update',NULL,2,NULL,1,133,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393091c88c11f18a38286ed489eee1','1483688293013995521','字典项删除',NULL,NULL,NULL,NULL,'sys:dictItem:delete',NULL,2,NULL,1,139,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973930c8c88c11f18a38286ed489eee1','1483688293013995521','字典项详情',NULL,NULL,NULL,NULL,'sys:dictItem:info',NULL,2,NULL,1,136,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973930fdc88c11f18a38286ed489eee1','1483688293013995521','字典项列表',NULL,NULL,NULL,NULL,'sys:dictItem:list',NULL,2,NULL,1,135,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393132c88c11f18a38286ed489eee1','1483688293013995521','字典项新增',NULL,NULL,NULL,NULL,'sys:dictItem:save',NULL,2,NULL,1,137,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393167c88c11f18a38286ed489eee1','1483688293013995521','字典项修改',NULL,NULL,NULL,NULL,'sys:dictItem:update',NULL,2,NULL,1,138,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973931a3c88c11f18a38286ed489eee1','1491245732043280385','日志列表',NULL,NULL,NULL,NULL,'sys:log:list',NULL,2,NULL,1,140,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973931dcc88c11f18a38286ed489eee1','1475705421636411394','菜单删除',NULL,NULL,NULL,NULL,'sys:menu:delete',NULL,2,NULL,1,115,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393217c88c11f18a38286ed489eee1','1475705421636411394','菜单详情',NULL,NULL,NULL,NULL,'sys:menu:info',NULL,2,NULL,1,112,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393251c88c11f18a38286ed489eee1','1475705421636411394','菜单列表',NULL,NULL,NULL,NULL,'sys:menu:list',NULL,2,NULL,1,110,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393289c88c11f18a38286ed489eee1','1475705421636411394','菜单新增',NULL,NULL,NULL,NULL,'sys:menu:save',NULL,2,NULL,1,113,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973932c0c88c11f18a38286ed489eee1','1475705421636411394','菜单选择',NULL,NULL,NULL,NULL,'sys:menu:select',NULL,2,NULL,1,111,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393311c88c11f18a38286ed489eee1','1475705421636411394','菜单修改',NULL,NULL,NULL,NULL,'sys:menu:update',NULL,2,NULL,1,114,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393349c88c11f18a38286ed489eee1','1490853450420862977','系统指标',NULL,NULL,NULL,NULL,'sys:monitor:metrics',NULL,2,NULL,1,160,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393380c88c11f18a38286ed489eee1','1490853450420862977','Redis 监控',NULL,NULL,NULL,NULL,'sys:monitor:redis',NULL,2,NULL,1,161,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973933bac88c11f18a38286ed489eee1','1475705421628022785','角色删除',NULL,NULL,NULL,NULL,'sys:role:delete',NULL,2,NULL,1,105,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973933f3c88c11f18a38286ed489eee1','1475705421628022785','角色详情',NULL,NULL,NULL,NULL,'sys:role:info',NULL,2,NULL,1,102,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739342cc88c11f18a38286ed489eee1','1475705421628022785','角色列表',NULL,NULL,NULL,NULL,'sys:role:list',NULL,2,NULL,1,100,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393466c88c11f18a38286ed489eee1','1475705421628022785','角色新增',NULL,NULL,NULL,NULL,'sys:role:save',NULL,2,NULL,1,103,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739349dc88c11f18a38286ed489eee1','1475705421628022785','角色选择',NULL,NULL,NULL,NULL,'sys:role:select',NULL,2,NULL,1,101,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973934d4c88c11f18a38286ed489eee1','1475705421628022785','角色修改',NULL,NULL,NULL,NULL,'sys:role:update',NULL,2,NULL,1,104,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739350ac88c11f18a38286ed489eee1','1491699116638507010','任务删除',NULL,NULL,NULL,NULL,'sys:schedule:delete',NULL,2,NULL,1,154,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739353fc88c11f18a38286ed489eee1','1491699116638507010','任务详情',NULL,NULL,NULL,NULL,'sys:schedule:info',NULL,2,NULL,1,151,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393575c88c11f18a38286ed489eee1','1491699116638507010','任务列表',NULL,NULL,NULL,NULL,'sys:schedule:list',NULL,2,NULL,1,150,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973935aac88c11f18a38286ed489eee1','1491699116638507010','任务日志',NULL,NULL,NULL,NULL,'sys:schedule:log',NULL,2,NULL,1,156,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973935e0c88c11f18a38286ed489eee1','1491699116638507010','任务执行',NULL,NULL,NULL,NULL,'sys:schedule:run',NULL,2,NULL,1,155,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('97393615c88c11f18a38286ed489eee1','1491699116638507010','任务新增',NULL,NULL,NULL,NULL,'sys:schedule:save',NULL,2,NULL,1,152,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739364ac88c11f18a38286ed489eee1','1491699116638507010','任务修改',NULL,NULL,NULL,NULL,'sys:schedule:update',NULL,2,NULL,1,153,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9739368bc88c11f18a38286ed489eee1','1475705421565108225','用户详情',NULL,NULL,NULL,NULL,'sys:user:info',NULL,2,NULL,1,130,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('973936c6c88c11f18a38286ed489eee1','1475705421565108225','重置用户密码',NULL,NULL,NULL,NULL,'sys:user:resetPassword',NULL,2,NULL,1,131,'security-hardening','2026-10-15 19:35:58',NULL,NULL,0),('9d8f3b45cdc711f1ae17286ed489eee1','1475705421628022785','分配角色权限',NULL,NULL,NULL,NULL,'sys:role:assignPermission',NULL,2,NULL,1,106,'security-hardening','2026-10-22 11:21:05',NULL,NULL,0);
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` varchar(32) NOT NULL,
  `role_name` varchar(100) DEFAULT NULL COMMENT '角色名称',
  `role_code` varchar(100) DEFAULT NULL COMMENT '角色编码',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `creator` varchar(20) DEFAULT NULL COMMENT '创建者ID',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `editor` varchar(32) DEFAULT NULL COMMENT '修改者',
  `mtime` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES ('1485529471914319874','Admin','admin','','admin','2022-01-24 16:26:41','admin','2026-10-22 11:26:58',0),('1485842742563291137','操作员','operate','','admin','2022-01-25 13:11:30','admin','2022-01-25 13:11:30',0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `id` varchar(32) NOT NULL,
  `role_id` varchar(32) DEFAULT NULL COMMENT '角色ID',
  `permission_id` varchar(32) DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与菜单对应关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES ('2114899216793260034','1485529471914319874','1475678576111116290'),('2114899216805842945','1485529471914319874','1475702165283561473'),('2114899216818425857','1485529471914319874','1475705421565108225'),('2114899216818425858','1485529471914319874','1493036989883158530'),('2114899216831008770','1485529471914319874','1493037125191405569'),('2114899216835203073','1485529471914319874','1493037227893133313'),('2114899216843591682','1485529471914319874','1493037339163824129'),('2114899216847785985','1485529471914319874','9739368bc88c11f18a38286ed489eee1'),('2114899216851980289','1485529471914319874','973936c6c88c11f18a38286ed489eee1'),('2114899216860368897','1485529471914319874','1475705421628022785'),('2114899216864563201','1485529471914319874','9739342cc88c11f18a38286ed489eee1'),('2114899216868757505','1485529471914319874','9739349dc88c11f18a38286ed489eee1'),('2114899216877146114','1485529471914319874','973933f3c88c11f18a38286ed489eee1'),('2114899216885534721','1485529471914319874','97393466c88c11f18a38286ed489eee1'),('2114899216889729025','1485529471914319874','973934d4c88c11f18a38286ed489eee1'),('2114899216893923330','1485529471914319874','973933bac88c11f18a38286ed489eee1'),('2114899216893923331','1485529471914319874','9d8f3b45cdc711f1ae17286ed489eee1'),('2114899216902311937','1485529471914319874','1475705421636411394'),('2114899216910700546','1485529471914319874','97393251c88c11f18a38286ed489eee1'),('2114899216919089154','1485529471914319874','973932c0c88c11f18a38286ed489eee1'),('2114899216927477762','1485529471914319874','97393217c88c11f18a38286ed489eee1'),('2114899216973615106','1485529471914319874','97393289c88c11f18a38286ed489eee1'),('2114899216986198017','1485529471914319874','97393311c88c11f18a38286ed489eee1'),('2114899216986198018','1485529471914319874','973931dcc88c11f18a38286ed489eee1'),('2114899216998780929','1485529471914319874','1483688293013995521'),('2114899217007169538','1485529471914319874','97392fd4c88c11f18a38286ed489eee1'),('2114899217011363842','1485529471914319874','97392f9ac88c11f18a38286ed489eee1'),('2114899217019752449','1485529471914319874','9739300dc88c11f18a38286ed489eee1'),('2114899217023946753','1485529471914319874','97393044c88c11f18a38286ed489eee1'),('2114899217023946754','1485529471914319874','97392f60c88c11f18a38286ed489eee1'),('2114899217032335361','1485529471914319874','973930fdc88c11f18a38286ed489eee1'),('2114899217040723969','1485529471914319874','973930c8c88c11f18a38286ed489eee1'),('2114899217053306882','1485529471914319874','97393132c88c11f18a38286ed489eee1'),('2114899217074278402','1485529471914319874','97393167c88c11f18a38286ed489eee1'),('2114899217074278403','1485529471914319874','97393091c88c11f18a38286ed489eee1'),('2114899217082667009','1485529471914319874','1485420753806172161'),('2114899217091055618','1485529471914319874','97392e73c88c11f18a38286ed489eee1'),('2114899217095249921','1485529471914319874','97392eebc88c11f18a38286ed489eee1'),('2114899217103638529','1485529471914319874','97392e12c88c11f18a38286ed489eee1'),('2114899217112027137','1485529471914319874','97392eb1c88c11f18a38286ed489eee1'),('2114899217120415745','1485529471914319874','97392f24c88c11f18a38286ed489eee1'),('2114899217124610050','1485529471914319874','97392b12c88c11f18a38286ed489eee1'),('2114899217132998657','1485529471914319874','1491245732043280385'),('2114899217141387266','1485529471914319874','973931a3c88c11f18a38286ed489eee1'),('2114899217149775874','1485529471914319874','1486979057737142274'),('2114899217153970177','1485529471914319874','1491699116638507010'),('2114899217162358786','1485529471914319874','97393575c88c11f18a38286ed489eee1'),('2114899217166553090','1485529471914319874','9739353fc88c11f18a38286ed489eee1'),('2114899217174941697','1485529471914319874','97393615c88c11f18a38286ed489eee1'),('2114899217179136002','1485529471914319874','9739364ac88c11f18a38286ed489eee1'),('2114899217179136003','1485529471914319874','9739350ac88c11f18a38286ed489eee1'),('2114899217191718914','1485529471914319874','973935e0c88c11f18a38286ed489eee1'),('2114899217195913217','1485529471914319874','973935aac88c11f18a38286ed489eee1'),('2114899217200107521','1485529471914319874','1490853450420862977'),('2114899217208496130','1485529471914319874','97393349c88c11f18a38286ed489eee1'),('2114899217216884737','1485529471914319874','97393380c88c11f18a38286ed489eee1'),('2114899217225273345','1485529471914319874','1486983766581579778'),('2115275525952278530','1485842742563291137','1475678576111116290'),('2115275525964861442','1485842742563291137','2053295662509240322'),('2115275525969055746','1485842742563291137','1475702165283561473'),('2115275525969055747','1485842742563291137','1475705421565108225'),('2115275525981638658','1485842742563291137','1493036989883158530'),('2115275525981638659','1485842742563291137','1493037125191405569'),('2115275525994221569','1485842742563291137','1493037227893133313'),('2115275525998415874','1485842742563291137','1493037339163824129'),('2115275525998415875','1485842742563291137','9739368bc88c11f18a38286ed489eee1'),('2115275526006804481','1485842742563291137','973936c6c88c11f18a38286ed489eee1'),('2115275526015193089','1485842742563291137','1475705421628022785'),('2115275526015193090','1485842742563291137','9739342cc88c11f18a38286ed489eee1'),('2115275526023581698','1485842742563291137','9739349dc88c11f18a38286ed489eee1'),('2115275526027776001','1485842742563291137','973933f3c88c11f18a38286ed489eee1'),('2115275526036164610','1485842742563291137','97393466c88c11f18a38286ed489eee1'),('2115275526036164611','1485842742563291137','973934d4c88c11f18a38286ed489eee1'),('2115275526048747522','1485842742563291137','973933bac88c11f18a38286ed489eee1'),('2115275526052941825','1485842742563291137','9d8f3b45cdc711f1ae17286ed489eee1'),('2115275526057136130','1485842742563291137','97392e73c88c11f18a38286ed489eee1'),('2115275526069719042','1485842742563291137','97392eebc88c11f18a38286ed489eee1');
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` varchar(32) NOT NULL,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `realname` varchar(255) DEFAULT NULL COMMENT '用户姓名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `salt` varchar(20) DEFAULT NULL COMMENT '盐',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(100) DEFAULT NULL COMMENT '手机号',
  `sex` int DEFAULT '0' COMMENT '性别(0保密1男性2女性)',
  `dept_code` varchar(32) DEFAULT NULL COMMENT '部门code',
  `user_type` int DEFAULT '1' COMMENT '0外部人员1内部人员',
  `del_flag` tinyint DEFAULT NULL COMMENT '状态  0：正常   1：禁用',
  `creator` varchar(32) DEFAULT NULL COMMENT '创建者',
  `ctime` datetime DEFAULT NULL COMMENT '创建时间',
  `editor` varchar(32) DEFAULT NULL COMMENT '修改人',
  `mtime` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES ('1','admin','管理员','$2a$10$vUPRl5a/qVTgpNn1JA/2Vu.06LaOsM/y2laTRyvNVMiDqtLyYSdCC','YzcmCZNvbXocrsz9dm8e','root@qq.com','13612345678',0,'1485429843877969921',1,0,'1','2016-11-11 11:11:11',NULL,'2024-03-07 15:38:08'),('1491744161173450754','test1','测试','$2a$10$L5l9T6uzaZ5L5mlxziBJmOOCPx.In0f4McS/qFYt.wmTVu86KIprG',NULL,'hahaha@qq.com','1333333333',1,'1485429924664459265',1,0,'admin','2022-02-10 20:01:38','admin','2026-10-28 15:23:50');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` varchar(32) NOT NULL,
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `role_id` varchar(32) DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与角色对应关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES ('1490965414576992257','1','1485529471914319874'),('2115343687737630721','1491744161173450754','1485529471914319874');
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'athena_boot'
--

--
-- Dumping routines for database 'athena_boot'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-28 15:27:34
