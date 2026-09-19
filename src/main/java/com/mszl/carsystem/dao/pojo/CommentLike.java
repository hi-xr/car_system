package com.mszl.carsystem.dao.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentLike {
    private Integer id;
    private Integer userId;
    private Integer commentId;
    private Date createTime;
}

