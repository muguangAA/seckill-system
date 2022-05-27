package com.muguang.core.config.rabbitmq;

import com.muguang.core.entity.MiaoshaUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MiaoshaMessage {
	private MiaoshaUser user;
	private long goodsId;
}
