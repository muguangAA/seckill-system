package com.muguang.order.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.muguang.core.entity.MiaoshaUser;
import com.muguang.core.entity.vo.GoodsDetailVo;
import com.muguang.core.entity.vo.GoodsVo;
import com.muguang.core.service.GoodsService;
import com.muguang.core.util.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Resource
	GoodsService goodsService;

	/**
	 * QPS:1267 load:15 mysql
	 * 5000 * 10
	 * QPS:2884, load:5 
	 *
	 * @return*/
    @RequestMapping(value="/to_list")
    public List<GoodsVo> list(HttpServletRequest request, HttpServletResponse response, MiaoshaUser user) {
		return goodsService.listGoodsVo();
    }

    @RequestMapping(value="/detail/{goodsId}")
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, MiaoshaUser user,
										@PathVariable("goodsId")long goodsId) {
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	long startAt = goods.getStartDate().getTime();
    	long endAt = goods.getEndDate().getTime();
    	long now = System.currentTimeMillis();
    	int miaoshaStatus = 0;
    	int remainSeconds = 0;
		//秒杀还没开始，倒计时
    	if(now < startAt ) {
    		remainSeconds = (int)((startAt - now )/1000);
		//秒杀已经结束
    	}else if(now > endAt){
    		miaoshaStatus = 2;
    		remainSeconds = -1;
		//秒杀进行中
    	}else {
    		miaoshaStatus = 1;
    	}
    	GoodsDetailVo vo = new GoodsDetailVo();
    	vo.setGoods(goods);
    	vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setMiaoshaStatus(miaoshaStatus);
    	return Result.success(vo);
    }
    
    
}
