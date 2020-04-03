/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.controller;

import com.demo.controller.vo.req.LoginVo;
import com.demo.controller.vo.res.CodeMsg;
import com.demo.controller.vo.res.ResVo;
import com.demo.dao.domain.Product;
import com.demo.service.ProductService;
import com.demo.service.UserService;
import com.github.xjs.audit.util.UUIDUtil;
import com.github.xjs.audit.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 登录 controller
 *
 * @author 605162215@qq.com
 * @date 2019/12/6 14:06
 **/
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResVo login(HttpServletResponse response, @Valid @RequestBody LoginVo vo){
        String tk = userService.login(response, vo);
        if(StringUtils.isEmpty(tk)){
            return ResVo.fail(CodeMsg.REQUEST_ILLEGAL);
        }else{
            return ResVo.ok(tk);
        }
    }

}
