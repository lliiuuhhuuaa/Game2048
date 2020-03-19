package com.sp.game2048.enums;

/***
 * 倒计时状态
 */
public enum PermissionRequestTypeEnum {
    READ_PHONE_STATE(1),
    ;
    private Integer value;
    PermissionRequestTypeEnum(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
