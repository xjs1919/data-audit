package com.github.xjs.audit.mybatis.util;

import com.github.xjs.audit.mybatis.SkipAudit;
import com.github.xjs.audit.util.StringUtil;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class MybatisUtils {

	public static final String CUSTOMQUERY = "_CUSTOMQUERY";
	public static final String FOR_EACH_KEY_PREFIX = "__frch_item_";
    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<>(0);

    /**
     * 根据MappedStatement构造查询MappedStatement
     * @param ms
     * */
    public static MappedStatement newHashMapMappedStatement(MappedStatement ms) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + CUSTOMQUERY, ms.getSqlSource(), ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), HashMap.class, EMPTY_RESULTMAPPING).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    /**
     * 获取方法参数
     * @param mappedStatement
     * @param boundSql
     * @param parameterObject
     * @return Map<String, Object>
     * */
    public static List<Map<String, Object>> getParameter(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject){
        Configuration configuration = mappedStatement.getConfiguration();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
        if(mappedStatement.getStatementType() == StatementType.PREPARED){
            if (parameterMappings != null) {
                for (int i = 0; i < parameterMappings.size(); i++) {
                    ParameterMapping parameterMapping = parameterMappings.get(i);
                        if (parameterMapping.getMode() != ParameterMode.OUT) {
                            Object value;
                        String propertyName = parameterMapping.getProperty();
                        if (boundSql.hasAdditionalParameter(propertyName)) {
                            value = boundSql.getAdditionalParameter(propertyName);
                        } else if (parameterObject == null) {
                            value = null;
                        } else if (mappedStatement.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
                            value = parameterObject;
                        } else {
                            MetaObject metaObject = configuration.newMetaObject(parameterObject);
                            value = metaObject.getValue(propertyName);
                        }
                        String indexedName = "";
                        int idx = getPropertyIndex(propertyName);
                        if(idx < 0){
                            indexedName = FOR_EACH_KEY_PREFIX + "0";
                        }else{
                            indexedName = getIndexedName(propertyName);
                        }
                        Map<String,Object> m = map.get(indexedName);
                        if(m == null){
                            m = new LinkedHashMap<String, Object>();
                            map.put(indexedName, m);
                        }
                        PropertyTokenizer token = new PropertyTokenizer(propertyName);
                        if(token.hasNext()){
                            m.put(token.getChildren(), value);
                        }else{
                            m.put(propertyName, value);
                        }
                    }
                }
            }
        }
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys, (key1, key2)->{
            int idx1 = getPropertyIndex(key1);
            int idx2 = getPropertyIndex(key2);
            return idx1-idx2;
        });
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for(String key : keys){
            Map<String, Object> value = map.get(key);
            if(value != null && value.size() > 0){
                list.add(value);
            }
        }
        return list;
    }

    /**
     * 获取dao原始方法上的SkipAudit注解
     *
     * @param mappedStatement MappedStatement
     * @return
     */

    public static SkipAudit getSkipAuditAnnotation(MappedStatement mappedStatement){
        return getAnnotation(mappedStatement, SkipAudit.class);
    }

    /**
     * 获取dao原始方法上的注解
     *
     * @param mappedStatement MappedStatement
     * @param annotationClass 注解的class对象
     * @return
     */
    public static <T extends Annotation> T getAnnotation(MappedStatement mappedStatement, Class<T> annotationClass){
        Method method = getMethod(mappedStatement);
        if(method == null){
            return null;
        }
        return method.getAnnotation(annotationClass);
    }

    /**
     * 获取dao原始方法
     *
     * @param mappedStatement MappedStatement
     * @return
     */
    public static Method getMethod(MappedStatement mappedStatement) {
        try {
            String id = mappedStatement.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            final Method[] method = Class.forName(className).getMethods();
            for (Method me : method) {
                if (me.getName().equals(methodName)) {
                    return me;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取属性的索引。
     * 如果是List<Bean>    propertyName = FOR_EACH_KEY_PREFIX+索引.bean的属性名
     * 如果是List<基本类型> propertyName = FOR_EACH_KEY_PREFIX+索引
     *
     * @param propertyName 属性名
     * @return
     */
    public static int getPropertyIndex(String propertyName){
        if(StringUtil.isEmpty(propertyName)){
            return -1;
        }
        if(!propertyName.startsWith(FOR_EACH_KEY_PREFIX)){
            return -1;
        }
        int dot = propertyName.indexOf(".");
        if(dot > 0){
            propertyName = propertyName.substring(0, dot);
        }
        String[] arr = propertyName.split("_");
        return Integer.parseInt(arr[arr.length-1]);
    }

    /**
     * 获取属性带索引的前缀部分。
     * 如果是List<Bean>    propertyName = FOR_EACH_KEY_PREFIX+索引.bean的属性名
     * 如果是List<基本类型> propertyName = FOR_EACH_KEY_PREFIX+索引
     * @param propertyName 属性名
     * @return FOR_EACH_KEY_PREFIX+索引
     */
    public static String getIndexedName(String propertyName){
        if(StringUtil.isEmpty(propertyName)){
            return propertyName;
        }
        if(!propertyName.startsWith(FOR_EACH_KEY_PREFIX)){
            return propertyName;
        }
        int dot = propertyName.indexOf(".");
        if(dot > 0){
            return propertyName.substring(0, dot);
        }else{
            return propertyName;
        }
    }

    /**
     * 下划线转驼峰
     *
     * @param oldKey 属性名
     * @return FOR_EACH_KEY_PREFIX+索引
     */
    public static String mapUnderscoreToCamelCase(String oldKey){
        if (oldKey == null || "".equals(oldKey.trim())) {
            return "";
        }
        int len = oldKey.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = oldKey.charAt(i);
            if (c == '_') {
                if (++i < len) {
                    sb.append(Character.toUpperCase(oldKey.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

