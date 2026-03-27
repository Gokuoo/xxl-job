package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlJobUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @author xuxueli 2019-05-04 16:44:59
 */
@Mapper
public interface XxlJobUserDao {

	public List<XxlJobUser> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("username") String username,
									 @Param("role") int role);
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("username") String username,
							 @Param("role") int role);

	public XxlJobUser loadByUserName(@Param("username") String username);

	// 新增：根据ID查询用户
	public XxlJobUser loadById(@Param("id") int id);

	public int save(XxlJobUser xxlJobUser);

	public int update(XxlJobUser xxlJobUser);
	
	public int delete(@Param("id") int id);

}
