package com.ssafy.vue.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vue.model.BaseAddressDto;
import com.ssafy.vue.model.UserLocDto;
import com.ssafy.vue.model.service.InterestService;
import com.ssafy.vue.model.MemberDto;
import com.ssafy.vue.model.service.JwtServiceImpl;
import com.ssafy.vue.model.service.MemberService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/user")
@Api("사용자 컨트롤러  API V1")
public class MemberController {

	public static final Logger logger = LoggerFactory.getLogger(MemberController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	
	@Autowired
	private JwtServiceImpl jwtService;

	@Autowired
	private MemberService memberService;

	@ApiOperation(value = "로그인", notes = "Access-token과 로그인 결과 메세지를 반환한다.", response = Map.class)
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@RequestBody @ApiParam(value = "로그인 시 필요한 회원정보(아이디, 비밀번호).", required = true) MemberDto memberDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		try {
			MemberDto loginUser = memberService.login(memberDto);
			if (loginUser != null) {
				String token = jwtService.create("userid", loginUser.getUserid(), "access-token");// key, data, subject
				logger.debug("로그인 토큰정보 : {}", token);
				resultMap.put("access-token", token);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.ACCEPTED;
			}
		} catch (Exception e) {
			logger.error("로그인 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	@ApiOperation(value = "회원가입", notes = "회원가입 결과를 가져온다", response = String.class)
	@PostMapping("/register")
	public ResponseEntity<String> registerMember(
			@RequestBody @ApiParam(value = "회원 가입 정보(아이디, 이름, 비밀번호, 이메일).", required = true) MemberDto memberDto) {
		HttpStatus status = HttpStatus.ACCEPTED;
		try {
			MemberDto user = memberService.userInfo(memberDto.getUserid());
			if (user == null && memberService.registerMember(memberDto)) {
				logger.info("회원가입 성공 : {}", memberDto.getUserid());
				return new ResponseEntity<String>(SUCCESS, HttpStatus.OK);
			}
			else {
				logger.error("회원가입 실패 : {} 존재", user.getUserid());
				return new ResponseEntity<String>("duplicate", status);
			}
		} catch (Exception e) {
			logger.error("회원가입 실패 : {}", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<String>(FAIL, status);
	}
	
	@ApiOperation(value = "회원정보 수정", notes = "회원정보 수정을 한다", response = String.class)
	@PostMapping("/update")
	public ResponseEntity<String> updateMember(
			@RequestBody @ApiParam(value = "회원 수정 정보(이름, 비밀번호, 이메일).", required = true) MemberDto memberDto) {
		HttpStatus status = HttpStatus.ACCEPTED;
		try {
			MemberDto user = memberService.userInfo(memberDto.getUserid());
			if (user != null && memberService.updateMember(memberDto)) {
				logger.info("회원정보 수정 성공 : {}", memberDto.getUserid());
				return new ResponseEntity<String>(SUCCESS, HttpStatus.OK);
			}
			else {
				logger.error("회원정보 수정 실패 : {} 없음", user.getUserid());
			}
		} catch (Exception e) {
			logger.error("회원정보 수정 실패 : {}", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<String>(FAIL, status);
	}
	
	@ApiOperation(value = "회원인증", notes = "회원 정보를 담은 Token을 반환한다.", response = Map.class)
	@GetMapping("/info/{userid}")
	public ResponseEntity<Map<String, Object>> getInfo(
			@PathVariable("userid") @ApiParam(value = "인증할 회원의 아이디.", required = true) String userid,
			HttpServletRequest request) {
//		logger.debug("userid : {} ", userid);
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = HttpStatus.ACCEPTED;
		if (jwtService.isUsable(request.getHeader("access-token"))) {
			logger.info("사용 가능한 토큰!!!");
			try {
//				로그인 사용자 정보.
				MemberDto memberDto = memberService.userInfo(userid);
				resultMap.put("userInfo", memberDto);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} catch (Exception e) {
				logger.error("정보조회 실패 : {}", e);
				resultMap.put("message", e.getMessage());
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			logger.error("사용 불가능 토큰!!!");
			resultMap.put("message", FAIL);
			status = HttpStatus.ACCEPTED;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	@ApiOperation(value = "회원인증", notes = "회원 정보를 담은 Token을 반환한다.", response = Map.class)
	@DeleteMapping("/info/{userid}")
	public ResponseEntity<String> deleteMember(
			@PathVariable("userid") @ApiParam(value = "인증할 회원의 아이디.", required = true) String userid,
			HttpServletRequest request) {
//		logger.debug("userid : {} ", userid);
		String result = FAIL;
		HttpStatus status = HttpStatus.ACCEPTED;
		try {
			if (memberService.deleteMember(userid)) {
				logger.info("회원 삭제 완료 : {}", userid);
				result = SUCCESS;
			}
		} catch (Exception e) {
			logger.error("정보조회 실패 : {}", e);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<String>(result, status);
	}
	
	@ApiOperation(value = "회원정보 리스트", notes = "회원 정보를 담은 리스트을 반환한다.", response = List.class)
	@GetMapping("/list")
	public ResponseEntity<List<MemberDto>> listMember(HttpServletRequest request) {
		HttpStatus status = HttpStatus.OK;
		try {
			logger.info("회원정보 조회");
			return new ResponseEntity<List<MemberDto>>(memberService.listMember(), status);
		} catch (Exception e) {
			logger.error("회원정보 조회 에러");
			return null;
		}
	}
	
	@Autowired
	private InterestService interestService;
	
	@ApiOperation(value = "관심 목록 등록", notes = "관심 목록을 등록한다.", response = String.class)
	@PostMapping("/interest/register")
	public ResponseEntity<String> interest(@RequestBody @ApiParam(value = "관심목록 등록를 하기 위한 정보", required = true) UserLocDto userLocDto) {
		logger.info(userLocDto.getUserid());
		logger.info(userLocDto.getDongcode());
		try {
			if (interestService.interestCheck(userLocDto) == 0) {
				logger.info(userLocDto.getUserid());
				logger.info(userLocDto.getDongcode());
				interestService.registerInterest(userLocDto);
				logger.info("관심 목록 등록 성공 : {}", userLocDto.getUserid());
				return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);
			}
			else {
				logger.info("dddd");
				interestService.plusHit(userLocDto);
				logger.info("관심 목록 조회+1 성공 : {}", userLocDto.getUserid());
				return new ResponseEntity<String>(SUCCESS, HttpStatus.ACCEPTED);
			}
		} catch (Exception e) {
			logger.error("관심 목록 등록 에러 : {}", e);
			return new ResponseEntity<String>(FAIL, HttpStatus.ACCEPTED);
		}
	}
	
	@ApiOperation(value = "관심 목록", notes = "관심 목록을 반환한다.", response = List.class)
	@PostMapping("/interest/list")
	public ResponseEntity<List<BaseAddressDto>> interests(@RequestBody @ApiParam(value = "관심목록 조회를 하기 위한 정보", required = true) UserLocDto userLocDto) throws Exception {
		return new ResponseEntity<List<BaseAddressDto>>(interestService.getInterests(userLocDto.getUserid()), HttpStatus.OK);
	}

	@ApiOperation(value = "top 관심 목록", notes = "top 관심 목록을 반환한다.", response = List.class)
	@GetMapping("/interest/top")
	public ResponseEntity<List<BaseAddressDto>> tops() throws Exception {
		return new ResponseEntity<List<BaseAddressDto>>(interestService.getTops(), HttpStatus.OK);
	}

}
