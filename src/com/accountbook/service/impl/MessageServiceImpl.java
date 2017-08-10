package com.accountbook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accountbook.dao.MessageDao;
import com.accountbook.modle.Message;
import com.accountbook.service.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService{
	
	@Autowired
	MessageDao dao;

	@Override
	public void newMessage(Message data) {
		dao.insert(data);
	}

	@Override
	public Message findMessage(int id) {
		return dao.queryById(id);
	}

	@Override
	public int getUnreadCount() {
		return dao.queryUnreadCount();
	}

	@Override
	public void makeReaded(int id) {
		dao.updateStatus(id, Message.STATUS_READED);
	}

	@Override
	public void makeDeleted(int id) {
		dao.updateStatus(id, Message.STATUS_DELETE);
	}

	@Override
	public void makeAccepted(int id) {
		dao.updateStatus(id, Message.STATUS_INVITE_ACCEPT);
	}

	@Override
	public void makeRefused(int id) {
		dao.updateStatus(id, Message.STATUS_INVITE_REFUSE);
	}

}