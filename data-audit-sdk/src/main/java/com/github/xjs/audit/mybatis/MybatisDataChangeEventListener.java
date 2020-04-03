/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.mybatis;

import com.github.xjs.audit.mybatis.dto.ChangeData;
import com.github.xjs.audit.mybatis.dto.ChangeRowData;
import com.github.xjs.audit.threadlocal.AuditDataHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MybatisDataChangeEventListener
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 14:42
 **/
@Slf4j
public class MybatisDataChangeEventListener implements OnDataChangeEventListener {

    /**插入*/
    public static final int TYPE_INSERT = 1;

    /**更新*/
    public static final int TYPE_UPDATE = 2;

    /**删除*/
    public static final int TYPE_DELETE = 3;

    @Override
    public void onDelete(List<ChangeRowData> changeRows) {
        if(log.isDebugEnabled()){
            log.debug("onDelete:{}", changeRows);
        }
        if(changeRows == null || changeRows.size() <= 0){
            return;
        }
        AuditDataHolder.addToDetails(changeRowsToDetails(changeRows, TYPE_DELETE));
    }

    @Override
    public void onInsert(List<ChangeRowData> changeRows) {
        if(log.isDebugEnabled()){
            log.debug("onInsert:{}", changeRows);
        }
        if(changeRows == null || changeRows.size() <= 0){
            return;
        }
        AuditDataHolder.addToDetails(changeRowsToDetails(changeRows, TYPE_INSERT));
    }

    @Override
    public void onUpdate(List<ChangeRowData> changeRows) {
        if(log.isDebugEnabled()){
            log.debug("onUpdate:{}", changeRows);
        }
        if(changeRows == null || changeRows.size() <= 0){
            return;
        }
        AuditDataHolder.addToDetails(changeRowsToDetails(changeRows, TYPE_UPDATE));
    }

    /**
     * 把变化的数据组装成服务端需要的数据
     * @param changeRows 变化的数据
     * @param sqlType 操作类型，1:add，2:update，3:delete
     * */
    private List<Detail> changeRowsToDetails(List<ChangeRowData> changeRows, int sqlType){
        List<Detail> details = new ArrayList<Detail>(changeRows.size());
        for(ChangeRowData row : changeRows){
            Detail detail = new Detail();
            detail.setSchemaName(row.getSchemaName());
            detail.setTableName(row.getTableName());
            detail.setSqlType(sqlType);
            Map<String, ChangeData> changeMap = row.getChangeColumnMap();
            List<ChangeData> diffData = mapToList(changeMap);
            if(diffData == null || diffData.size() <= 0){
                continue;
            }
            detail.setDiff(diffData);
            details.add(detail);
        }
        return details;
    }

    private List<ChangeData> mapToList(Map<String, ChangeData> changeMap){
        if(changeMap == null || changeMap.size() <= 0){
            return null;
        }
        List<ChangeData> list = new ArrayList<ChangeData>(changeMap.size());
        for(Map.Entry<String, ChangeData> entry : changeMap.entrySet()){
            ChangeData value = entry.getValue();
            list.add(value);
        }
        return list;
    }

    /**
     * 发生变化的数据详情
     * */
    @Data
    public static class Detail{
        /**sql类型，1插入，2更新，3删除*/
        private int sqlType;
        /**库名*/
        private String schemaName;
        /**表名*/
        private String tableName;
        /**变化的数据列表*/
        private List<ChangeData> diff;
    }

}
