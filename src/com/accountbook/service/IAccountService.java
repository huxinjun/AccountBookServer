package com.accountbook.service;

import java.util.List;

import com.accountbook.model.Account;
import com.accountbook.model.Member;
import com.accountbook.model.PayTarget;

public interface IAccountService {
	
	/**
	 * 记账
	 * @param account
	 */
	public void addNewAccount(Account account);
	public void addMember(Member member);
	public void addPayTarget(PayTarget target);
	
	public List<Member> findAllMembers(String userId);
	public List<Account> findAccounts(String userId);
	public List<Account> findAccounts(String userId,String bookId);
	
}
