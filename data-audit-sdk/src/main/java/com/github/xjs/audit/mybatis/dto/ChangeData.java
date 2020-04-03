/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.mybatis.dto;
/**
 * 变化的数据
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 16:39
 **/
public class ChangeData {

    /**列名*/
    private String name;
    /**旧值*/
    private Object oldValue;
    /**新值*/
    private Object newValue;


    /**中文列名*/
    private String nameCn;

    public ChangeData(){}

    public ChangeData(String name, Object oldValue, Object newValue) {
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    @Override
    public String toString() {
        return "ChangeData{" +
                "name='" + name + '\'' +
                ", oldValue=" + oldValue +
                ", newValue=" + newValue +
                '}';
    }
}
