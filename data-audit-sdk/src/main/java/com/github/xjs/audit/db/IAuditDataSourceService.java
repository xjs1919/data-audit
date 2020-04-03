/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.db;

import javax.sql.DataSource;
import java.util.List;

/**
 * IAuditDataSourceService
 * 目前支持的数据源：<br/>
 * <ul>
 *     <li>Hikari</li>
 *     <li>dbcp2</li>
 *     <li>tomcat</li>
 *     <li>druid</li>
 *     <li>mybatis内置数据源</li>
 * </ul>
 * 如果应用需要用到其他类型的数据源，请联系605162215@qq.com进行添加。
 * @author 605162215@qq.com
 * @date 2019/12/12 18:22
 **/
public interface IAuditDataSourceService {

    /**
     * 获取要审计的数据源，可能有多个
     * */
    List<DataSource> getDataSources();


}
