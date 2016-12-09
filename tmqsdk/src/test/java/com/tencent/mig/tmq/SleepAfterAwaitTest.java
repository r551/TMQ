package com.tencent.mig.tmq;

import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by yoyoqin on 2016/12/5.
 */
public class SleepAfterAwaitTest extends BaseTest {
    /**
     * 在同一个测试用例中切换预期模式，同时测试TMQ.awaitAndSleep接口是否生效
     * @throws Exception
     */
    @Test
    public void testSleepAfterAwait1() throws Exception {
        // 严格模式
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        long startTime1 = System.currentTimeMillis();
        TMQ.awaitAndSleep(AWAIT_TIMEOUT, 5);
        assertTrue(System.currentTimeMillis() - startTime1 > 5*1000);
        assertTrue(TMQ.check());
        TMQ.printHistory();

        // 切换到松散模式，不在乎顺序
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest1", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        long startTime2 = System.currentTimeMillis();
        TMQ.awaitAndSleep(AWAIT_TIMEOUT, 5);
        assertTrue(System.currentTimeMillis() - startTime1 > 5*1000);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());
        TMQ.printHistory();

        // 切换到严格模式
        TMQ.switchExpectMode(ModeEnum.STRICT);
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.awaitAndSleep(AWAIT_TIMEOUT, 5);
        assertTrue(System.currentTimeMillis() - startTime1 > 5*1000);
        assertTrue(TMQ.check());
        TMQ.printHistory();

        // 切换到松散模式，不在乎顺序
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest1", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.awaitAndSleep(AWAIT_TIMEOUT, 5);
        assertTrue(System.currentTimeMillis() - startTime1 > 5*1000);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());
        TMQ.printHistory();
    }
}
