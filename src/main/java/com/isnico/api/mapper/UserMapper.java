package com.isnico.api.mapper;

import com.isnico.api.domain.po.User;
import com.isnico.api.domain.vo.user.UserRestVo;
import org.apache.ibatis.annotations.Param;
import org.nico.ourbatis.mapper.SimpleMapper;

public interface UserMapper extends SimpleMapper<User, String>{

	public UserRestVo selectRestUseInfo(@Param("userId") String userId);
}
