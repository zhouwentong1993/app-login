package com.wentong.app.controller;

import com.wentong.app.mapper.LoginMapper;
import com.wentong.app.vo.LoginRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.wentong.app.common.Constant.ONE_DAY_IN_MILL_SECONDS;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private LoginMapper loginMapper;

    @GetMapping("normal/{userId}")
    public String login(@PathVariable String userId) {
        Objects.requireNonNull(userId);
        long now = System.currentTimeMillis();
        long nowInMillSeconds = now - now % ONE_DAY_IN_MILL_SECONDS;
        LoginRecord loginRecordQuery = new LoginRecord();
        loginRecordQuery.setUserId(userId);
        Example example = new Example(LoginRecord.class);
        example.createCriteria().andEqualTo("userId", userId);
        example.setOrderByClause("timestamp desc");
        List<LoginRecord> loginRecords = loginMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(loginRecords)) {
            newLoginRecord(userId, nowInMillSeconds);
            return "insert new data";
        } else {
            LoginRecord loginRecord = loginRecords.get(0);
            // 代表当天没有插入数据
            if (loginRecord.getTimestamp() < nowInMillSeconds) {
                // 根据移位的时间判断是否应该新起一行
                long passedDays = (nowInMillSeconds - loginRecord.getTimestamp()) / ONE_DAY_IN_MILL_SECONDS;
                String s = Integer.toBinaryString(loginRecord.getLoginData());
                // 31 = Integer.toBinaryString(Integer.MAX_VALUE);
                int maxMovedTime = 31 - s.length();
                // 超过了移位操作的最大程度，整数会出现溢出
                if (passedDays > maxMovedTime) {
                    newLoginRecord(userId, nowInMillSeconds);
                    return "insert new data";
                } else {
                    loginRecord.setLoginData((loginRecord.getLoginData() << passedDays) + 1);
                    loginRecord.setTimestamp(nowInMillSeconds);
                    loginRecord.setUpdateTime(new Date());
                    loginMapper.updateByPrimaryKeySelective(loginRecord);
                    return "update data";
                }
            }
            return "maintain data";
        }

    }

    private void newLoginRecord(@PathVariable String userId, long nowInMillSeconds) {
        LoginRecord loginRecord = new LoginRecord();
        loginRecord.setUserId(userId);
        loginRecord.setTimestamp(nowInMillSeconds);
        loginRecord.setLoginData(1);
        loginRecord.setCreateTime(new Date());
        loginRecord.setUpdateTime(new Date());
        loginMapper.insert(loginRecord);
    }

    public static void main(String[] args) {
        int i = 3;
        System.out.println(Integer.toBinaryString(i));
        System.out.println(Integer.toBinaryString(Integer.MAX_VALUE).length());

    }

    /**
     * 求最多左移多少次，不至于超出 Integer.MAX_VALUE
     */
    private static int maxMovedTime(int source) {
        int target = Integer.MAX_VALUE - 1;
        if (source >= target || (source << 1) >= target) {
            return 0;
        }
        int counter = 1;
        while ((source << 1) <= target) {
            source = source << 1;
            counter++;
        }
        return counter;
    }

}
