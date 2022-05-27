package com.muguang.shopping.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.muguang.core.entity.vo.LoginVo;
import com.muguang.core.util.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.muguang.core.service.MiaoshaUserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

	private static Logger log = LoggerFactory.getLogger(LoginController.class);

	@Resource
	MiaoshaUserService miaoshaUserService;

    @PostMapping("/do_login")
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
    	//登录
    	String token = miaoshaUserService.login(response, loginVo);
    	return Result.success(token);
    }
}
