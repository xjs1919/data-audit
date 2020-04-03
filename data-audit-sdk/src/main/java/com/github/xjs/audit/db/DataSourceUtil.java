/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.xjs.audit.util.DBUtil;
import com.github.xjs.audit.util.StringUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * 数据源工具类
 *
 * @author 605162215@qq.com
 * @date 2019/12/19 14:58
 **/
@Slf4j
public class DataSourceUtil {

    /**
     * 获取数据源所连接的数据库的名字
     * @param schemaInSql sql语句中的schema
     * @param dataSource 数据源
     * */
    public static String getSchemaName(String schemaInSql, DataSource dataSource) {
        //sql语句中的优先级最高
        if(!StringUtil.isEmpty(schemaInSql)){
            return schemaInSql;
        }
        //然后从url中获取
        String url = "";
        String defaultCatalog = "";
        String defaultSchema = "";
        String dataSourceClassName = dataSource.getClass().getName();
        //可能生成动态代理的子类
        if(dataSourceClassName.indexOf("com.zaxxer.hikari.HikariDataSource") >= 0){
            //Hikari数据源
            HikariDataSource hds = (HikariDataSource)dataSource;
            url = hds.getJdbcUrl();
            defaultCatalog = hds.getCatalog();
            defaultSchema = hds.getSchema();
        }else if(dataSourceClassName.indexOf("org.apache.commons.dbcp2.BasicDataSource") >= 0){
            //dbcp2数据源
            BasicDataSource bds = (BasicDataSource)dataSource;
            url = bds.getUrl();
            defaultCatalog = bds.getDefaultCatalog();
            defaultSchema = bds.getDefaultSchema();
        }else if(dataSourceClassName.indexOf("org.apache.tomcat.jdbc.pool.DataSource") >= 0){
            //tomcat数据源
            org.apache.tomcat.jdbc.pool.DataSource tds = (org.apache.tomcat.jdbc.pool.DataSource)dataSource;
            url = tds.getUrl();
            defaultCatalog = tds.getDefaultCatalog();
        }else if(dataSourceClassName.indexOf("org.apache.ibatis.datasource.pooled.PooledDataSource") >= 0){
            //mybatis内置的数据源
            PooledDataSource pds = (PooledDataSource)dataSource;
            url = pds.getUrl();
        }else if(dataSourceClassName.indexOf("com.alibaba.druid.pool.DruidDataSource") >= 0){
            //druid数据源
            DruidDataSource dds = (DruidDataSource)dataSource;
            url = dds.getUrl();
            defaultCatalog = dds.getDefaultCatalog();
        }else if(dataSourceClassName.indexOf("com.mchange.v2.c3p0.ComboPooledDataSource") >= 0){
            //c3p0数据源
            ComboPooledDataSource cds = (ComboPooledDataSource)dataSource;
            url = cds.getJdbcUrl();
        }else if(AbstractRoutingDataSource.class.isAssignableFrom(dataSource.getClass())){
            DataSource ds = retrieveFromDynamic(dataSource);
            if(ds != null){
                return getSchemaName(schemaInSql, ds);
            }
        } else{
            log.error("不支持的数据源:{}", dataSourceClassName);
        }
        if(!StringUtil.isEmpty(url)){
            String schema = DBUtil.getDbNameFromUrl(url);
            if(!StringUtil.isEmpty(schema)){
                return schema;
            }
        }
        if(!StringUtil.isEmpty(defaultCatalog)){
            return defaultCatalog;
        }
        if(!StringUtil.isEmpty(defaultSchema)){
            return defaultSchema;
        }
        return null;
    }

    private static DataSource retrieveFromDynamic(final DataSource dataSource){
        //如果是boss的动态数据源
        Class<?> dataSourceClass = dataSource.getClass();
        try{
            Method getDsMethod = ReflectionUtils.findMethod(dataSourceClass, "determineTargetDataSource", null);
            getDsMethod.setAccessible(true);
            return (DataSource)getDsMethod.invoke(dataSource, null);
        }catch(Exception e){
            log.error(e.getMessage(), e);
            log.error("动态数据源{}获取真实数据源异常:{}", dataSource.getClass().getName(),e);
            return null;
        }
    }

}
