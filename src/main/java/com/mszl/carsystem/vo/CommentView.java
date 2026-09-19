package com.mszl.carsystem.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentView {
    private Integer commentId;
    private Integer userId;
    private String userName;
    private String avatar;
    private Long schemeId;
    private String content;
    private Integer parentId;
    private Integer rootId;
    private Integer floorLevel;
    private Integer likeCount;
    private Date createdAt;
}

