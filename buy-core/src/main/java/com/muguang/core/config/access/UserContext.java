package com.muguang.core.config.access;


import com.muguang.core.entity.MiaoshaUser;

public class UserContext {
	
	private static final ThreadLocal<MiaoshaUser> USER_HOLDER = new ThreadLocal<MiaoshaUser>();
	
	public static void setUser(MiaoshaUser user) {
		USER_HOLDER.set(user);
	}
	
	public static MiaoshaUser getUser() {
		return USER_HOLDER.get();
	}

	public static void removeUser() {
		USER_HOLDER.remove();
	}

}
