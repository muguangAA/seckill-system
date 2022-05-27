package com.muguang.core.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.muguang.core.util.result.CodeMsg;
import com.muguang.core.util.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

	@ExceptionHandler(BindException.class)
	public Result<String> bindExceptionHandler(HttpServletRequest request, BindException e) {
		e.printStackTrace();
		List<ObjectError> errors = e.getAllErrors();
		ObjectError error = errors.get(0);
		String msg = error.getDefaultMessage();
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
	}

	@ExceptionHandler(GlobalException.class)
	public Result<String> globalExceptionHandler(HttpServletRequest request, GlobalException e) {
		e.printStackTrace();
		return Result.error(e.getCm());
	}

	@ExceptionHandler(Exception.class)
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		return Result.error(CodeMsg.SERVER_ERROR);
	}
}
