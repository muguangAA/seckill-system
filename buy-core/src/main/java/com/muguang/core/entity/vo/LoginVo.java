package com.muguang.core.entity.vo;

import javax.validation.constraints.NotNull;

import com.muguang.core.util.validator.IsMobile;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {
	
	@NotNull
	@IsMobile
	private String mobile;
	
	@NotNull
	@Length(min=32)
	private String password;
}
