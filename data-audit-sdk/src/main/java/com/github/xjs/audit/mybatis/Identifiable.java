/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.mybatis;
/**
 * 标识接口,实现了这个接口的类是有id字段的
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 13:56
 **/
public interface Identifiable<T> {
    public T getId();
    public void setId(T t);
}
