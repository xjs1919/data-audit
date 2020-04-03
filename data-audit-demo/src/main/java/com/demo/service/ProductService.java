/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.service;

import com.demo.dao.ProductMapper;
import com.demo.dao.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 9:38
 **/
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Product getById(Long id){
        return productMapper.selectById(id);
    }

    public List<Product> listAll() {
        return productMapper.listAll();
    }

    public List<Product> listByIds(List<Long> ids){
        return productMapper.listByIds(ids);
    }

    public int insert(Product product){
        return productMapper.insert(product);
    }

    public int update(Product product){
        return productMapper.update(product);
    }

    public int delete(Product product){
        return productMapper.delete(product);
    }

    public int insertBatch(List<Product> products){
        return productMapper.batchInsert(products);
    }

    public int insertBatch2(List<Product> products){
        return productMapper.batchInsert2(products);
    }

    public int deleteBatch(List<Long> ids){
        return productMapper.deleteBatch(ids);
    }

    public int deleteBatch2(List<Product> ps) {
        return productMapper.deleteBatch2(ps);
    }

    public int updateBatch(List<Product> ps) {
        return productMapper.updateBatch(ps);
    }
    public int updateBatch2(List<Product> ps) {
        return productMapper.updateBatch2(ps);
    }

    public int incrPrice(Product product){
        return productMapper.incrPrice(product);
    }

    public int incrByName(String name){
        return productMapper.incrByName(name);
    }
}
