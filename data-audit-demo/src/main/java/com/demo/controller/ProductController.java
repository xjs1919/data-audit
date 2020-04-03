/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.controller;

import com.demo.dao.domain.Product;
import com.demo.service.ProductService;
import com.demo.controller.vo.res.ResVo;
import com.github.xjs.audit.config.DataAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品controller
 *
 * @author 605162215@qq.com
 * @date 2019/12/6 14:06
 **/
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService prodService;

    @GetMapping("/info")
    public ResVo info(@Valid Long id){
        Product product = prodService.getById(id);
        return ResVo.ok(product);
    }

    @GetMapping("/list")
    public ResVo list(){
        List<Product> products = prodService.listAll();
        return ResVo.ok(products);
    }

    @DataAudit(eventId="1", eventName = "添加商品")
    @PostMapping("/add")
    public ResVo add(@RequestBody Product prod){
        prodService.insert(prod);
        return ResVo.ok(prod.getId());
    }

    @DataAudit(eventId="1", eventName = "修改商品")
    @PostMapping("/update")
    public ResVo update(@RequestBody Product prod){
        int ret = prodService.update(prod);
        return ResVo.ok(ret > 0);
    }

    @DataAudit(eventId="1", eventName = "删除商品")
    @PostMapping("/delete")
    public ResVo delete(@RequestBody Product prod){
        int ret = prodService.delete(prod);
        return ResVo.ok(ret > 0);
    }

}
