package com.sp.game2048.socket.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class SocketMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer code;
	private String msg;
	private Object body;//消息内容
	public SocketMessage(){}
	public SocketMessage(Integer code){
		this.code = code;
	}
	public SocketMessage(Integer code, String msg){
		this.code = code;
		this.msg = msg;
	}
	public SocketMessage(Integer code, Object body){
		this.code = code;
		this.body = body;
	}
}
