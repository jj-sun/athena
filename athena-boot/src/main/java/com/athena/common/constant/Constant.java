package com.athena.common.constant;

/**
 * 常量
 *
 * @author Mr.sun
 */
public class Constant {
	/** 超级管理员ID */
	public static final String SUPER_ADMIN = "admin";

    public static final String BASE_MENU_NAME = "首页";

    public static final String TREE_ROOT = "0";
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     *  升序
     */
    public static final String ASC = "asc";

    /**
     * 字典翻译文本后缀
     */
    public static final String DICT_TEXT_SUFFIX = "_dictText";

	/**
	 * 菜单类型
	 */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private final int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * 定时任务状态
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    public enum DelFlag {
        /**
         * 正常
         */
        DEL_FLAG_0(0),
        /**
         * 禁用
         */
        DEL_FLAG_1(1);

        private final int value;
        DelFlag(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

    }

    public enum PermissionType {
        /**
         * 权限类型
         */
        LIST(0, "目录"),
        MENU(1, "菜单"),
        BUTTON(2, "按钮");
        private final int value;
        private final String name;
        PermissionType(int value, String name) {
            this.value = value;
            this.name = name;
        }
        public int getValue() {
            return value;
        }

    }

    public enum LogType {
        /**
         * 日志类型
         */
        LOGIN(0, "登录日志"),
        OPERATE(1, "操作日志");
        private final int value;
        private final String name;
        LogType(int value, String name) {
            this.value = value;
            this.name = name;
        }
        public int getValue() {
            return value;
        }
    }
}
