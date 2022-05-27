package com.muguang.shopping.controller;

import com.muguang.core.config.redis.RedisService;
import com.muguang.core.entity.MiaoshaUser;
import com.muguang.core.service.MiaoshaUserService;
import com.muguang.core.util.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(MiaoshaUser user) {
        return Result.success(user);
    }
    
}
