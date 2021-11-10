package com.isnico.api.controller;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.isnico.api.consts.CacheConst;
import com.isnico.api.consts.SystemConst;
import com.isnico.api.domain.po.Role;
import com.isnico.api.domain.po.User;
import com.isnico.api.domain.vo.ResponseCode;
import com.isnico.api.domain.vo.ResponseVo;
import com.isnico.api.domain.vo.user.UserFullVo;
import com.isnico.api.utils.CommonUtils;
import com.isnico.api.utils.EncryptUtils;
import org.hibernate.validator.constraints.Length;
import org.nico.noson.util.string.StringUtils;
import com.isnico.api.component.CacheComponent;
import com.isnico.api.service.RoleService;
import com.isnico.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CacheComponent cacheComponent;

	@ApiOperation(value = "用户注册")
	@PostMapping("/register")
	public ResponseVo<UserFullVo> register(
			@ApiParam(value = "账户", required = true) @NotNull @RequestParam String account,
			@ApiParam(value = "密码", required = true) @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$", message = "密码需要由长度为6~15的字母和数字组成") @RequestParam String password,
			@ApiParam(value = "昵称", required = true) @Length(min = 3, max = 15) @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$", message = "昵称只能为字母、汉字或数字") @RequestParam String nickname,
			@ApiParam(value = "验证码", required = true) @Length(min = SystemConst.MAIL_AUTH_CODE_LENGTH, max = SystemConst.MAIL_AUTH_CODE_LENGTH) @RequestParam String authCode
			){
		ResponseCode code = null;
		UserFullVo userFullVo = null;

		String cacheAuthCode = cacheComponent.get(CacheConst.TRAP_USER_REGISTER_AUTH_CODE + account);
		if(StringUtils.isNotBlank(cacheAuthCode)) {
			if(authCode.equalsIgnoreCase(cacheAuthCode)) {
				User user = new User();
				user.setId(CommonUtils.getUUID());
				user.setAccount(account);
				user.setNickname(nickname);
				user.setPassword(EncryptUtils.encode(password));

				Role defaultRole = roleService.selectDefaultRole();
				user.setRuleId(defaultRole.getId());
				user.setRuleName(defaultRole.getName());
				user.setRuleType(defaultRole.getType());

				int modify = userService.insert(user);
				if(modify == 1) {
					userFullVo = CommonUtils.convertPovo(user, UserFullVo.class);
					
					code = ResponseCode.SUCCESS;
					cacheComponent.delete(CacheConst.TRAP_USER_REGISTER_AUTH_CODE + account);
				}else {
					code = ResponseCode.ERROR_ON_INSERT;
				}
			}else {
				code = ResponseCode.ERROR_ON_AUTH_CODE_AUTH_FAIL;
			}
		}else {
			code = ResponseCode.ERROR_ON_AUTH_CODE_NOT_EXIST;
		}
		return new ResponseVo<>(code, userFullVo);
	}

	@ApiOperation(value = "用户登录")
	@PostMapping("/login")
	public ResponseVo<UserFullVo> login(
			@ApiParam(value = "账户", required = true) @NotNull @RequestParam String account,
			@ApiParam(value = "密码", required = true) @NotNull @RequestParam String password,
			@ApiParam(value = "验证码", required = false) @RequestParam(required = false) String authCode
			){
		ResponseCode code = null;
		UserFullVo userFullVo = null;
		
		String failCountCache = cacheComponent.get(CacheConst.TRAP_USER_PASSWORD_FAIL_COUNT + account);

		boolean access = true;
		int failCount = 0;
		
		if(failCountCache != null && (failCount = Integer.valueOf(failCountCache)) > 2) {
			String cacheAuthCode = cacheComponent.get(CacheConst.TRAP_USER_LOGIN_AUTH_CODE + account);
			if(StringUtils.isNotBlank(cacheAuthCode)) {
				if(! authCode.equalsIgnoreCase(cacheAuthCode)) {
					access = false;
					code = ResponseCode.ERROR_ON_AUTH_CODE_AUTH_FAIL;
				}else {
					cacheComponent.delete(CacheConst.TRAP_USER_REGISTER_AUTH_CODE + account);
				}
			}else {
				access = false;
				code = ResponseCode.ERROR_ON_AUTH_CODE_NOT_EXIST;
			}
		}
		
		if(access) {
			User user = userService.selectEntity(new User().setAccount(account));
			if(user != null) {
				if(user.getPassword().equalsIgnoreCase(EncryptUtils.encode(password))) {
					userFullVo = CommonUtils.convertPovo(user, UserFullVo.class);
					
					cacheComponent.delete(CacheConst.TRAP_USER_PASSWORD_FAIL_COUNT + account);
					code = ResponseCode.SUCCESS;
				}else {
					cacheComponent.setEx(CacheConst.TRAP_USER_PASSWORD_FAIL_COUNT + account, String.valueOf(failCount + 1), 3L, TimeUnit.MINUTES);
					code = ResponseCode.ERROR_ON_USER_PASSWORD_ERROR;
				}
			}else {
				cacheComponent.setEx(CacheConst.TRAP_USER_PASSWORD_FAIL_COUNT + account, String.valueOf(failCount + 1), 3L, TimeUnit.MINUTES);
				code = ResponseCode.ERROR_ON_USER_NOT_EXIST;
			}
		}
				
		return new ResponseVo<>(code, userFullVo);
	}





}
