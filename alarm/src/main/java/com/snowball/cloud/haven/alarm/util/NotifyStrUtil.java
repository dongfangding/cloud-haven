package com.snowball.cloud.haven.alarm.util;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2024/06/13 11:17
 */
public class NotifyStrUtil {

    /**
     * 获取按月分表的基础表名， 如history_202406,则返回history
     *
     * @param tableName
     * @return
     */
    public String getOriginBaseTableNameFromMonth(String tableName) {
        return tableName.substring(0, tableName.lastIndexOf("_"));
    }

}
