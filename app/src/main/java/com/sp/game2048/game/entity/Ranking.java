package com.sp.game2048.game.entity;

import lombok.Data;

/**
 * @author lh
 * @do
 * @date 2019-09-17 19:41
 */
@Data
public class Ranking {
    private Long id;
    /**
     * 名称
     */
    private Long userId;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 分数
     */
    private Integer score;
    /**
     * 创建时间
     */
    private Long createDate;
    /**
     * 更新时间
     */
    private Long updateDate;
    /**
     * 排名
     */
    private Integer ranking;
}
