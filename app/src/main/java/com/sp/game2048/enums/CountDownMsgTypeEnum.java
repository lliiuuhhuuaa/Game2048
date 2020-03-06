package com.sp.game2048.enums;

/***
 * 倒计时状态
 */
public enum CountDownMsgTypeEnum {
    UPDATE_NUMBER(1),
    GAME_OVER(2),
    ;
    private Integer value;
    CountDownMsgTypeEnum(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
