package com.wentong.app;

import org.junit.jupiter.api.Test;

public class NormalTest {

    @Test
    public void testTime() {
        long now = System.currentTimeMillis();
        System.out.println(now);
        System.out.println(now % 86400000);
        System.out.println(now - now % 86400000);
    }

    @Test
    public void testMoveBit() {
        int i = 1;
        System.out.println((i << 1) + 1);
    }

}
