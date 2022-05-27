package com.muguang.core.entity.vo;

import com.muguang.core.entity.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {
	private GoodsVo goods;
	private OrderInfo order;
}
