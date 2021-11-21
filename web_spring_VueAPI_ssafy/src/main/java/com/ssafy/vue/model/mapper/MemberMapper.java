package com.ssafy.vue.model.mapper;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.vue.model.MemberDto;

@Mapper
public interface MemberMapper {

	public MemberDto login(MemberDto memberDto) throws SQLException;
	public MemberDto userInfo(String userid) throws SQLException;
	public boolean registerMember(MemberDto memberDto) throws SQLException;
	public boolean updateMember(MemberDto memberDto) throws SQLException;
	public boolean deleteMember(String userid) throws SQLException;
	public List<MemberDto> listMember() throws SQLException;
	
}
