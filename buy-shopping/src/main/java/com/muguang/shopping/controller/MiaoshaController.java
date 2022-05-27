package com.muguang.shopping.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.muguang.core.config.access.AccessLimit;
import com.muguang.core.config.rabbitmq.MQSender;
import com.muguang.core.config.rabbitmq.MiaoshaMessage;
import com.muguang.core.config.redis.GoodsKey;
import com.muguang.core.config.redis.RedisService;
import com.muguang.core.entity.MiaoshaOrder;
import com.muguang.core.entity.MiaoshaUser;
import com.muguang.core.entity.vo.GoodsVo;
import com.muguang.core.service.MiaoshaService;
import com.muguang.core.service.OrderService;
import com.muguang.core.util.result.CodeMsg;
import com.muguang.core.util.result.Result;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.muguang.core.service.GoodsService;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Resource
	RedisService redisService;

	@Resource
	GoodsService goodsService;

	@Resource
	OrderService orderService;

	@Resource
	MiaoshaService miaoshaService;

	@Resource
	MQSender sender;

	/**
	 * 做内存标记，减少redis访问
	 */
	private final HashMap<Long, Boolean> localOverMap = new HashMap<>();
	
	/**
	 * 系统初始化
	 **/
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if(goodsList == null) {
			return;
		}
		for(GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
	
	@RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset() {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
//		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
//		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}

	@PostMapping(value="/do_miaosha")
	@ResponseBody
	public Result<Integer> miaosha(MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//验证path
//		boolean check = miaoshaService.checkPath(user, goodsId, path);
//		if(!check){
//			return Result.error(CodeMsg.REQUEST_ILLEGAL);
//		}
		//内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
		if(over) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
		if(stock <= 0) {
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		//排队中
		return Result.success(0);
	}


//	/**
//	 * QPS:1306
//	 * 5000 * 10
//	 * QPS: 2114
//	 * */
//    @RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
//    @ResponseBody
//    public Result<Integer> miaosha(MiaoshaUser user,
//    		@RequestParam("goodsId")long goodsId,
//    		@PathVariable("path") String path) {
//    	if(user == null) {
//    		return Result.error(CodeMsg.SESSION_ERROR);
//    	}
//    	//验证path
//    	boolean check = miaoshaService.checkPath(user, goodsId, path);
//    	if(!check){
//    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
//    	}
//    	//内存标记，减少redis访问
//    	boolean over = localOverMap.get(goodsId);
//    	if(over) {
//    		return Result.error(CodeMsg.MIAO_SHA_OVER);
//    	}
//    	//预减库存
//    	long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
//    	if(stock <= 0) {
//			localOverMap.put(goodsId, true);
//    		return Result.error(CodeMsg.MIAO_SHA_OVER);
//    	}
//    	//判断是否已经秒杀到了
//    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//    	if(order != null) {
//    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
//    	}
//    	//入队
//    	MiaoshaMessage mm = new MiaoshaMessage();
//    	mm.setUser(user);
//    	mm.setGoodsId(goodsId);
//    	sender.sendMiaoshaMessage(mm);
//		//排队中
//    	return Result.success(0);
//    }
    
    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
    	return Result.success(result);
    }
    
    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId
//    		@RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    		) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
//    	boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
//    	if(!check) {
//    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
//    	}
    	String path = miaoshaService.createMiaoshaPath(user, goodsId);
    	return Result.success(path);
    }
    
    
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }
}
