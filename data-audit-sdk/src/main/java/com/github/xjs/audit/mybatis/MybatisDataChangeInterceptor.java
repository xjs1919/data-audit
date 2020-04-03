/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.mybatis;

import com.github.xjs.audit.mybatis.parser.IDataParser;
import com.github.xjs.audit.mybatis.parser.ParserFactory;
import com.github.xjs.audit.mybatis.dto.ChangeRowData;
import com.github.xjs.audit.mybatis.dto.MybatisInvocation;
import com.github.xjs.audit.mybatis.util.MybatisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.List;
import java.util.Properties;

/**
 * mybatis截器，拦截Executor的Update方法，即数据库的更新操作，Insert，Update，Delete都会拦截
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 9:27
 **/
@Slf4j
@Intercepts(value = {
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisDataChangeInterceptor implements Interceptor {

    private OnDataChangeEventListener listener;

    public MybatisDataChangeInterceptor(OnDataChangeEventListener listener){
        this.listener = listener;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 拦截目标
        Object target = invocation.getTarget();
        Object result = null;
        if (target instanceof Executor) {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            String commandName = ms.getSqlCommandType().name();
            IDataParser dataParser = ParserFactory.getDataParser(commandName);
            MybatisInvocation mybatisInvocation = new MybatisInvocation(args, ms, parameter, (Executor) target);
            //如果是跳过审计
            SkipAudit skipAudit = MybatisUtils.getSkipAuditAnnotation(ms);
            if(skipAudit != null && skipAudit.skip()){
                return invocation.proceed();
            }
            boolean error = false;
            List<ChangeRowData> changeRows = null;
            try{
                // 1. 方法执行之前解析数据
                changeRows = dataParser.parseBefore(commandName, mybatisInvocation);
            }catch(Exception e){
                error = true;
                log.error(e.getMessage(), e);
            }
            // 2. 执行Update方法，除了查询之外的Insert，Delete，Update都是属于Update方法
            result = invocation.proceed();
            // 3. 方法执行之后处理数据,方法执行成功才需要记录差量
            if(result instanceof Number){
                Number ret = (Number)result;
                int retInt = ret.intValue();
                if(retInt <= 0 || error){
                    return result;
                }
                try{
                    changeRows = dataParser.parseAfter(mybatisInvocation, changeRows);
                    if (changeRows != null) {
                        onDataChanged(commandName, changeRows);
                    }
                }catch(Exception e){
                    log.error(e.getMessage(), e);
                }
            }else{
                log.error("Executor.update() return:{}", result);
            }
        }
        return result;
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 向外传递发生变化的数据
     * */
    private void onDataChanged(String commandName, List<ChangeRowData> changeRows) {
        if(this.listener == null){
            log.debug(changeRows.toString());
        }else{
            if (DBActionTypeEnum.UPDATE.getValue().equalsIgnoreCase(commandName)) {
                this.listener.onUpdate(changeRows);
            } else if (DBActionTypeEnum.INSERT.getValue().equalsIgnoreCase(commandName)) {
                this.listener.onInsert(changeRows);
            } else if (DBActionTypeEnum.DELETE.getValue().equalsIgnoreCase(commandName)) {
                this.listener.onDelete(changeRows);
            }
        }
    }

}
