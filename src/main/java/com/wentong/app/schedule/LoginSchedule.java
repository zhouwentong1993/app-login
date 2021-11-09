package com.wentong.app.schedule;

import com.wentong.app.mapper.LoginMapper;
import com.wentong.app.vo.LoginRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

import static com.wentong.app.common.Constant.ONE_DAY_IN_MILL_SECONDS;

public class LoginSchedule {

    @Autowired
    private LoginMapper loginMapper;

    /**
     每天固定时间扫描登录情况，判断是否连续五天登录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void execute() {
        long now = System.currentTimeMillis();
        long nowInMillSeconds = now - now % ONE_DAY_IN_MILL_SECONDS;
        long yesterdayInMillSeconds = nowInMillSeconds - ONE_DAY_IN_MILL_SECONDS;
        Example example = new Example(LoginRecord.class);
        example.createCriteria().andBetween("timestamp", yesterdayInMillSeconds, nowInMillSeconds);
        // 查询出所有昨天或者今天登录的用户
        List<LoginRecord> loginRecords = loginMapper.selectByExample(example);
        for (LoginRecord loginRecord : loginRecords) {
            Integer loginData = loginRecord.getLoginData();
            // 可能用户的数据正好跨月了
            if (loginData < 31) {
                // 不连续，比如 101.这种就不需要再判断了。
                if (Integer.toBinaryString(loginData).contains("0")) {
                    return;
                }
                LoginRecord loginRecordQuery = new LoginRecord();
                loginRecordQuery.setTimestamp(loginRecord.getTimestamp() - ONE_DAY_IN_MILL_SECONDS);
                loginRecordQuery.setUserId(loginRecord.getUserId());
                LoginRecord lastLoginRecord = loginMapper.selectOne(loginRecordQuery);
                if (lastLoginRecord != null) {
                    String binaryString = Integer.toBinaryString(lastLoginRecord.getLoginData());
                    String lastFiveDays = binaryString.substring(binaryString.length() - 5);
                    loginData = Integer.parseInt(lastFiveDays + Integer.toBinaryString(loginData), 2);
                }
            }
            // 代表连续五天登录，发放奖励
            if ((loginData & 31) == 31) {
                // send message queue to pride this user.
            }
        }
    }

}
