package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private Integer id;
    private Integer userId;
    private Long schemeId;
    private String content;

    private Integer parentId;
    private Integer rootId;
    private Integer floorLevel;

    private Integer likeCount;
    private Date createTime;
    private Date updateTime;
    private Integer isDeleted;
}

