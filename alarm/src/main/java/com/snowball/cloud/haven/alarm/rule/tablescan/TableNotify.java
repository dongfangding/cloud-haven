package com.snowball.cloud.haven.alarm.rule.tablescan;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/04 11:30
 */
public interface TableNotify {

    /**
     * 通知不存在的表名
     *
     * @param info
     */
    void notifyNotExistTables(TableNotExistNotifyInfo info);

    /**
     * 通知自动建表情况
     *
     * @param info
     */
    void notifyAuthCreateTable(TableAutoCreateNotifyInfo info);
}
