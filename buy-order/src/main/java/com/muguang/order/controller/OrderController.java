package com.muguang.order.controller;

import com.muguang.core.entity.MiaoshaUser;
import com.muguang.core.entity.OrderInfo;
import com.muguang.core.entity.vo.GoodsVo;
import com.muguang.core.entity.vo.OrderDetailVo;
import com.muguang.core.service.OrderService;
import com.muguang.core.util.result.CodeMsg;
import com.muguang.core.util.result.Result;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.muguang.core.service.GoodsService;

import javax.annotation.Resource;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Resource
	OrderService orderService;

	@Resource
	GoodsService goodsService;
	
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(MiaoshaUser user,
									  @RequestParam("orderId") long orderId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	OrderInfo order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return Result.error(CodeMsg.ORDER_NOT_EXIST);
    	}
    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);
    	return Result.success(vo);
    }
    
}
