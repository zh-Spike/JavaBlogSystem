package net.blog.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 管理CountDownLatch
 * 获取
 * 删除
 */
@Component
public class CountDownLatchManager {

    Map<String, CountDownLatch> latches = new HashMap<>();

    public void onPhoneDoLogin(String loginId) {
        CountDownLatch countDownLatch = latches.get(loginId);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public CountDownLatch getLatch(String loginId) {
        CountDownLatch countDownLatch = latches.get(loginId);
        if (countDownLatch == null) {
            countDownLatch = new CountDownLatch(1);
            latches.put(loginId, countDownLatch);
        }
        return countDownLatch;
    }

    public void deleteLatch(String loginId) {
        latches.remove(loginId);
    }

}