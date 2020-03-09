package com.sp.game2048.enums;

/***
 * 倒计时状态
 */
public enum CountDownMsgTypeEnum {
    UPDATE_NUMBER(1),
    GAME_OVER(2),
    PLAY_SOUND_COMPOUND(3),
    VIBRATE(4),
    START_321(5),
    ;
    private Integer value;
    CountDownMsgTypeEnum(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
