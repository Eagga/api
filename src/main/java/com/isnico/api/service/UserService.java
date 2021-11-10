package com.isnico.api.service;

import java.util.List;

import com.isnico.api.domain.po.User;
import com.isnico.api.domain.vo.user.UserRestVo;
import com.isnico.api.mapper.UserMapper;
import org.apache.ibatis.annotations.Param;
import org.nico.ourbatis.entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService{
	
	@Autowired
	private UserMapper userMapper;
	
	public User selectById(String key) {
		return userMapper.selectById(key);
	}

	public User selectEntity(User condition) {
		return userMapper.selectEntity(condition);
	}

	public List<User> selectList(User condition) {
		return userMapper.selectList(condition);
	}

	public long selectCount(Object condition) {
		return userMapper.selectCount(condition);
	}

	public List<User> selectPage(Page<Object> page) {
		return userMapper.selectPage(page);
	}

	public String selectId(User condition) {
		return userMapper.selectId(condition);
	}

	public List<String> selectIds(User condition) {
		return userMapper.selectIds(condition);
	}

	public int insert(User entity) {
		return userMapper.insert(entity);
	}

	public int insertSelective(User entity) {
		return userMapper.insertSelective(entity);
	}

	public int insertBatch(List<User> list) {
		return userMapper.insertBatch(list);
	}

	public int update(User entity) {
		return userMapper.update(entity);
	}

	public int updateSelective(User entity) {
		return userMapper.updateSelective(entity);
	}

	public int updateBatch(List<User> list) {
		return userMapper.updateBatch(list);
	}

	public int delete(User condition) {
		return userMapper.delete(condition);
	}

	public int deleteById(String key) {
		return userMapper.deleteById(key);
	}

	public int deleteBatch(List<String> list) {
		return userMapper.deleteBatch(list);
	}

	/**
	 * 通过userId查询用户简单信息
	 * 	- 头像
	 * 	- 昵称
	 * 
	 * @param userId 用户Id
	 * @return {@link UserRestVo}
	 */
	public UserRestVo selectRestUseInfo(String userId) {
		return userMapper.selectRestUseInfo(userId);
	}
	
}
