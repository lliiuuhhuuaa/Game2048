package com.sp.game2048.entity;

import lombok.Data;

/**
 * 手机信息
 */
@Data
public class PhoneInfo {
    /**
     * 手机号
     */
    private String phone;
    /**
     * 手机唯一标识
     */
    private String mac;
    /**
     * token
     */
    private String token;
}
