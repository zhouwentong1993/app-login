package com.wentong.app.mapper;

import com.wentong.app.vo.LoginRecord;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface LoginMapper extends Mapper<LoginRecord> {
}
