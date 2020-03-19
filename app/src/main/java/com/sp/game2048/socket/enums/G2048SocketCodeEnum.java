package com.sp.game2048.socket.enums;

public enum G2048SocketCodeEnum {

	GET_RANKING(1001), //排名
	UPDATE_SCORE(1002), //更新分数
	LIST_RANKING(1003), //分数列表
	MY_SCORE_HISTORY(1004), //我的分数历史
	;
	private Integer value;

	G2048SocketCodeEnum(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}
	/**
	 * 获取元素
	 * @param value
	 * @return
	 */
	public static G2048SocketCodeEnum getEnum(Integer value){
		G2048SocketCodeEnum[] values = G2048SocketCodeEnum.values();
		for(G2048SocketCodeEnum em : values){
			if(em.getValue().equals(value)){
				return em;
			}
		}
		return null;
	}
}
