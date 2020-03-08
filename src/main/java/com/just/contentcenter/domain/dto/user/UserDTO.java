package com.just.contentcenter.domain.dto.user;

import lombok.Data;

import java.util.Date;

/**
 * @author yanghao
 * @date 2020/1/19 14:14
 */
@Data
public class UserDTO {


    private Integer id;

    private String wxId;

    private String wxNickname;

    private String roles;

    private String avatarUrl;

    private Date createTime;

    private Date updateTime;

    private Integer bonus;


}
