package com.muguang.core.config.rabbitmq;

import com.muguang.core.config.redis.RedisService;
import com.muguang.core.entity.MiaoshaOrder;
import com.muguang.core.entity.MiaoshaUser;
import com.muguang.core.entity.vo.GoodsVo;
import com.muguang.core.service.GoodsService;
import com.muguang.core.service.MiaoshaService;
import com.muguang.core.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MQReceiver {

		@Resource
		GoodsService goodsService;

		@Resource
		OrderService orderService;

		@Resource
        MiaoshaService miaoshaService;

		@RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
		public void receive(String message) {
			MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
			MiaoshaUser user = mm.getUser();
			long goodsId = mm.getGoodsId();
			
			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	    	int stock = goods.getStockCount();
	    	if(stock <= 0) {
	    		return;
	    	}
	    	//判断是否已经秒杀到了，防止超买
	    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	    	if(order != null) {
	    		return;
	    	}
	    	//减库存 下订单 写入秒杀订单
	    	miaoshaService.miaosha(user, goods);
		}
}
