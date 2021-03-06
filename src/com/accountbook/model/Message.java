package com.accountbook.model;

import java.sql.Timestamp;

/**
 * 消息
 * @author xinjun
 *
 */
public class Message {
	
	public static final int MESSAGE_TYPE_SYSTEM=0;
	public static final int MESSAGE_TYPE_INVITE_USER=1;
	public static final int MESSAGE_TYPE_INVITE_GROUP=2;
	public static final int MESSAGE_TYPE_ACCOUNT=3;
	public static final int MESSAGE_TYPE_ACCOUNT_CREATE=31;
	public static final int MESSAGE_TYPE_ACCOUNT_CREATE_INNER=32;
	public static final int MESSAGE_TYPE_ACCOUNT_SETTLE=33;
	public static final int MESSAGE_TYPE_OTHER=99;
	
	
	public static final int STATUS_UNREAD=0;
	public static final int STATUS_READED=1;
	public static final int STATUS_DELETE=2;
	
	public static final int STATUS_INVITE_ACCEPT=11;
	public static final int STATUS_INVITE_REFUSE=12;
	

	public String id;
	public String fromId;
	public String toId;
	public int type;
	public String content;
	/**
	 * 创建时间 :毫秒
	 */
	public Timestamp time;
	
	/**
	 * 普通消息:0未读,1已读,2已删除
	 * 邀请消息:0未处理,1接受,2拒绝,3删除
	 */
	public int state;

	@Override
	public String toString() {
		return "Message [id=" + id + ", fromId=" + fromId + ", toId=" + toId + ", type=" + type + ", content=" + content
				+ ", time=" + time + ", state=" + state + "]\n";
	}
	
	
	
	
}
