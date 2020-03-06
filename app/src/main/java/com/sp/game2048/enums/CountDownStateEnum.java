package com.sp.game2048.enums;

/***
 * 倒计时状态
 */
public enum CountDownStateEnum {
    START(1),
    PAUSE(0);
    private Integer state;
    CountDownStateEnum(Integer state){
        this.state = state;
    }

    public Integer getState() {
        return state;
    }
}
