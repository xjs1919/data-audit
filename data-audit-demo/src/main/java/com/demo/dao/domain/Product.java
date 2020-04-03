/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.dao.domain;

import com.github.xjs.audit.mybatis.Identifiable;
import lombok.Data;

import java.util.Date;

/**
 * 商品实体类
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 9:40
 **/
@Data
public class Product implements Identifiable<Long> {
    /**商品ID*/
    private Long id;

    /**商品名称*/
    private String prodName;

    /**商品标题*/
    private String prodTitle;

    /**商品加价格*/
    private Integer prodPrice;

    /**商品详情*/
    private String prodDetail;

    private Date createTime;

    private Date updateTime;

    private Boolean enable;

    private Integer status;
}
