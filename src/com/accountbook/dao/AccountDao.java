package com.accountbook.dao;


import java.util.List;

import com.accountbook.modle.Account;
import com.accountbook.modle.Member;

public interface AccountDao {
	
	public void insert(Account account);
	
	/**查询和userId相关的所有成员:分组和帐友*/
	public List<Member> queryMembers(String userId);
}
