package com.tencent.mig.tmq;

import com.tencent.mig.tmq.model.IExpectModes;
import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlexibleModeTest extends BaseTest {

    @Test
    public void testFlexibleOneMessage() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 收到单条
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 收到多条
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    @Test
    public void testFlexibleMoreMessage() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 每个预期消息收1条
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 有的预期收到多条
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                // 下面UnitTest1的2号消息在这里写了两次，目的是UnitTest1的2号消息预期至少收到2条
                , new SimpleTmqMsg("UnitTest1", "2")
                , new SimpleTmqMsg("UnitTest1", "2")
        );
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest", "1"); // 多条
                TMQ.report("UnitTest", "2"); // 多条
                TMQ.report("UnitTest1", "1");
                // 发2条UnitTest1的2号消息
                TMQ.report("UnitTest1", "2");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 不在乎顺序
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
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 松散模式默认允许收到其他类型的消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest1", "2");
                // 其他类型消息
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest2", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 松散模式其实是只有预期消息没有全部至少收到1条时会校验不过
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
//                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertFalse(TMQ.check());
    }

    @Test
    public void testFlexibleZeroMessageExclusive() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 预期收不到任何消息
        TMQ.iCareWhatMsg(null);
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());

        // 预期收不到任何消息
        TMQ.iCareWhatMsg(SimpleTmqMsg.NULL);
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());

        // 预期收不到任何消息
        TMQ.iCareWhatMsg(SimpleTmqMsg.NULL);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 预期收不到任何消息
        TMQ.iCareWhatMsg(SimpleTmqMsg.KEY_MATCHED_NULL);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    @Test
    public void testFlexibleMoreMessageExclusive() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 预期收不到任何非预期消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.NULL // 最后一个是排他消息(只能放在最后)，代表预期收不到任何非预期消息
        );
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 第4个消息不在预期之中
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());

        // 预期收不到任何非预期消息，确实收不到应该校验通过
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.NULL // 最后一个是排他消息，代表预期收不到任何非预期消息
        );
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 第4个消息不在预期之中
//                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    /**
     * 另一种排他语义，在收全预期的消息后，不再接受其他消息，收全之前可以
     * @throws Exception
     */
    @Test
    public void testFlexibleMoreMessageKeyMatchedExclusive() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 收全预期的消息后，不再接受其他消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.KEY_MATCHED_NULL // 另一种排他消息(只能放在最后)，代表收全预期的消息后，不再接受其他消息
        );
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 第4个消息不在预期之中，此时预期的消息没收全，这条消息是允许的
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 收全预期的消息后，不再接受其他消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.KEY_MATCHED_NULL // 另一种排他消息(只能放在最后)，代表收全预期的消息后，不再接受其他消息
        );
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 第4个消息不在预期之中，此时预期的消息没收全，这条消息是允许的
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
                // 第6个消息不在预期之中，此时预期的消息已收全，这条消息是不允许的
                TMQ.report("UnitTest2", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());
    }

    /**
     * 设置只关注的消息类型后，收到的其他类型消息不再作为判定依据
     * @throws Exception
     */
    @Test
    public void testFlexibleMoreMessageFilter() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);

        // 预期收UnitTest和UnitTest1类的消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.NULL
        );

        // 过滤条件的设置只能放在预期消息方法iCareWhatMsg的后面，否则不会生效
        // 只关注"UnitTest", "UnitTest1","UnitTest2"消息，对于其他的消息不关注，出现也不作为判断依据
        TMQ.iCareWhatType("UnitTest", "UnitTest1", "UnitTest2");

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 不关注的消息，非预期，对结果判定无影响
                TMQ.report("UnitTest3", "1");
                // 关注的消息，但非预期，此消息会造成校验不通过
//                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 第二种情况，关注的消息，但非预期，会造成校验不通过
        // 预期收UnitTest和UnitTest1类的消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest1", "1")
                , new SimpleTmqMsg("UnitTest1", "2")
                , SimpleTmqMsg.NULL
        );

        // 过滤条件的设置只能放在预期消息方法iCareWhatMsg的后面，否则不会生效
        // 只关注"UnitTest", "UnitTest1","UnitTest2"消息，对于其他的消息不关注，出现也不作为判断依据
        TMQ.iCareWhatType("UnitTest", "UnitTest1", "UnitTest2");
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest1", "1");
                // 不关注的消息，非预期，对结果判定无影响
                TMQ.report("UnitTest3", "1");
                // 关注的消息，但非预期，此消息会造成校验不通过
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest1", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());
    }
}