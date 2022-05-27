package com.muguang.core.config.redis;

import java.util.*;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class RedisService {

	@Resource
	StringRedisTemplate stringRedisTemplate;

	/**
	 * 获取当个对象
	 * */
	public <T> T get(KeyPrefix prefix, String key,  Class<T> clazz) {
		 String realKey  = prefix.getPrefix() + key;
		 String str = stringRedisTemplate.opsForValue().get(realKey);
		return stringToBean(str, clazz);
	}
	
	/**
	 * 设置对象
	 * */
	public <T> boolean set(KeyPrefix prefix, String key,  T value) {
		 String str = beanToString(value);
		 if(str == null || str.length() <= 0) {
			 return false;
		 }
		//生成真正的key
		 String realKey  = prefix.getPrefix() + key;
		 int seconds =  prefix.expireSeconds();
		 if(seconds <= 0) {
			 stringRedisTemplate.opsForValue().set(realKey, str);
		 }else {
			 stringRedisTemplate.opsForValue().set(realKey, str, seconds);
		 }
		 return true;
	}
	
	/**
	 * 判断key是否存在
	 * */
	public <T> boolean exists(KeyPrefix prefix, String key) {
		//生成真正的key
		String realKey  = prefix.getPrefix() + key;
		return  stringRedisTemplate.opsForValue().get(realKey) != null;
	}
	
	/**
	 * 删除
	 * */
	public boolean delete(KeyPrefix prefix, String key) {
		//生成真正的key
		String realKey  = prefix.getPrefix() + key;
		return Boolean.TRUE.equals(stringRedisTemplate.delete(realKey));
	}
	
	/**
	 * 增加值
	 * */
	public <T> Long incr(KeyPrefix prefix, String key) {
		//生成真正的key
		String realKey  = prefix.getPrefix() + key;
		return stringRedisTemplate.opsForValue().increment(realKey);
	}
	
	/**
	 * 减少值
	 * */
	public <T> Long decr(KeyPrefix prefix, String key) {
		//生成真正的key
		String realKey = prefix.getPrefix() + key;
		return stringRedisTemplate.opsForValue().decrement(realKey);
	}
	
	public boolean delete(KeyPrefix prefix) {
		if(prefix == null) {
			return false;
		}
		Set<String> keys = scanMatch(prefix.getPrefix());
		if(keys ==null || keys.size() <= 0) {
			return true;
		}
		stringRedisTemplate.delete(keys);
		return true;
	}

	public Set<String> scanMatch(String matchKey) {
		Set<String> keys = new HashSet<>();
		RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
		RedisConnection redisConnection = null;
		if (connectionFactory != null) {
			redisConnection = connectionFactory.getConnection();
		}
		Cursor<byte[]> scan = null;
		if(redisConnection instanceof LettuceConnection){
			RedisClusterConnection clusterConnection = connectionFactory.getClusterConnection();
			Iterable<RedisClusterNode> redisClusterNodes = clusterConnection.clusterGetNodes();
			for (RedisClusterNode next : redisClusterNodes) {
				scan = clusterConnection.scan(next, ScanOptions.scanOptions().match(matchKey).count(Integer.MAX_VALUE).build());
				while (scan.hasNext()) {
					keys.add(new String(scan.next()));
				}
				scan.close();
			}
			return keys;
		}
		if(redisConnection instanceof JedisConnection){
			scan = redisConnection.scan(ScanOptions.scanOptions().match(matchKey).count(Integer.MAX_VALUE).build());
			while (scan.hasNext()){
				//找到一次就添加一次
				keys.add(new String(scan.next()));
			}
			scan.close();
			return keys;
		}
		return keys;

	}
	
	public static <T> String beanToString(T value) {
		if(value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if(clazz == int.class || clazz == Integer.class) {
			 return ""+value;
		}else if(clazz == String.class) {
			 return (String)value;
		}else if(clazz == long.class || clazz == Long.class) {
			return ""+value;
		}else {
			return JSON.toJSONString(value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T stringToBean(String str, Class<T> clazz) {
		if(str == null || str.length() <= 0 || clazz == null) {
			 return null;
		}
		if(clazz == int.class || clazz == Integer.class) {
			 return (T)Integer.valueOf(str);
		}else if(clazz == String.class) {
			 return (T)str;
		}else if(clazz == long.class || clazz == Long.class) {
			return  (T)Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}
}
