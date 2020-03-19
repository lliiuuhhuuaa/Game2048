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
    //弹Sweet消息
    ALERT_MSG(6),
    //弹系统提示
    ALERT_TOAST(7),
    //弹Sweet对象
    ALERT_SWEET(8),
    //回调
    CALL_BACK(9),
    ;
    private Integer value;
    CountDownMsgTypeEnum(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
