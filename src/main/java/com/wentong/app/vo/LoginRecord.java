package com.wentong.app.vo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "login_record")
public class LoginRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    @Column(name = "`timestamp`")
    private Long timestamp;
    private Integer loginData;
    private Date createTime;
    private Date updateTime;
}
