package com.sp.game2048.user.enums;

public enum SmsTypeEnum {

	FIND("find"),//找回密码
	LOGIN("login"),//登陆
	REGISTER("register"),//注册
;
	private String value;

	SmsTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	/**
	 * 获取元素
	 * @param value
	 * @return
	 */
	public static SmsTypeEnum getEnum(String value){
		SmsTypeEnum[] values = SmsTypeEnum.values();
		for(SmsTypeEnum em : values){
			if(em.getValue().equals(value)){
				return em;
			}
		}
		return null;
	}
}
