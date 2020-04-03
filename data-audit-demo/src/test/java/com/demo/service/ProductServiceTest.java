/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.service;

import com.demo.dao.ProductMapper;
import com.demo.dao.domain.Product;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 *  ProductServiceTest
 *
 * @author 605162215@qq.com
 * @date 2019/12/10 10:24
 **/
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Test
    public void listByIdsTest(){
        List<Long> ids = new ArrayList<Long>();
        ids.add(16L);
        ids.add(17L);
        List<Product> products = productService.listByIds(ids);
        log.info(""+(products == null?"0":products.size()));
    }

    @Test
    public void insertTest(){
        Product p = new Product();
        p.setProdName("name_1");
        p.setProdTitle("title_1");
        p.setProdPrice(101);
        p.setProdDetail("detail_1");
        p.setStatus(3);
        int ret = productService.insert(p);
        log.info("ret:"+ret+",id="+p.getId());
    }

    @Test
    public void updateTest(){
        Product p = new Product();
        p.setProdName("name_2");
        p.setProdTitle("title_24");
        p.setProdPrice(24);
        p.setProdDetail("detail_24");
        p.setStatus(3);
        p.setId(103L);
        int ret = productService.update(p);
        log.info("ret:"+ret);
    }

    @Test
    public void deleteTest(){
        Product p = new Product();
        p.setId(2L);
        int ret = productService.delete(p);
        log.info("ret:"+ret);
    }

    @Test
    public void batchInsertTest(){
        List<Product> ps = new ArrayList<Product>();
        Product p1 = new Product();
        p1.setProdName("name1");
        p1.setProdTitle("title1");
        p1.setProdPrice(100);
        p1.setProdDetail("detaildetail1");
        ps.add(p1);
        Product p2 = new Product();
        p2.setProdName("name2");
        p2.setProdTitle("title2");
        p2.setProdPrice(200);
        p2.setProdDetail("detaildetail2");
        ps.add(p2);
        int ret = productService.insertBatch(ps);
        log.info("ret:"+ret+",p1.id="+p1.getId()+",p2.id="+p2.getId());
    }

    @Test
    public void batchInsert2Test(){
        List<Product> ps = new ArrayList<Product>();
        Product p1 = new Product();
        p1.setProdName("name1");
        p1.setProdTitle("title1");
        p1.setProdPrice(100);
        p1.setProdDetail("detaildetail1");
        ps.add(p1);
        Product p2 = new Product();
        p2.setProdName("name2");
        p2.setProdTitle("title2");
        p2.setProdPrice(200);
        p2.setProdDetail("detaildetail2");
        ps.add(p2);
        int ret = productService.insertBatch2(ps);
        log.info("ret:"+ret+",p1.id="+p1.getId()+",p2.id="+p2.getId());
    }

    @Test
    public void deleteBatchTest(){
        List<Product> ps = new ArrayList<Product>();
        Product p1 = new Product();
        p1.setId(4L);
        ps.add(p1);

        Product p2 = new Product();
        p2.setId(5L);
        ps.add(p2);

        int ret = productService.deleteBatch2(ps);
        log.info("ret:"+ret);
    }

    @Test
    public void deleteBatch2Test(){
        List<Long> ids = new ArrayList<Long>();
        ids.add(6L);
        ids.add(7L);
        int ret = productService.deleteBatch(ids);
        log.info("ret:"+ret);
    }

    @Test
    public void updateBatchTest(){
        List<Product> ps = new ArrayList<Product>();
        Product p1 = new Product();
        p1.setProdName("name55-8");
        p1.setProdTitle("title55");
        p1.setProdPrice(55);
        p1.setProdDetail("detaildetail55");
        p1.setId(8L);
        ps.add(p1);
        Product p2 = new Product();
        p2.setProdName("name56-8");
        p2.setProdTitle("title56");
        p2.setProdPrice(56);
        p2.setProdDetail("detaildetail56");
        p2.setId(9L);
        ps.add(p2);;
        int ret = productService.updateBatch(ps);
        log.info("ret:"+ret);
    }


    @Test
    public void updateBatch2Test(){
        List<Product> ps = new ArrayList<Product>();
        Product p1 = new Product();
        p1.setProdName("name48-8");
        p1.setProdTitle("title48");
        p1.setProdPrice(48);
        p1.setProdDetail("detaildetail48");
        p1.setId(8L);
        ps.add(p1);
        Product p2 = new Product();
        p2.setProdName("name49-9");
        p2.setProdTitle("title49");
        p2.setProdPrice(49);
        p2.setProdDetail("detaildetail49");
        p2.setId(9L);
        ps.add(p2);;
        int ret = productService.updateBatch2(ps);
        log.info("ret:"+ret);
    }

    @Test
    public void updatePriceTest(){
        Product p = new Product();
        p.setId(8L);
        productService.incrPrice(p);
    }

    @Test
    public void incrManyPriceTest(){
        productService.incrByName("name");
    }

}
