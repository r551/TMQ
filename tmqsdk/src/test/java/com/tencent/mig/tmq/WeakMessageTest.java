package com.tencent.mig.tmq;

import com.tencent.mig.tmq.model.CheckListener;
import com.tencent.mig.tmq.model.IRetCode;
import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;
import com.tencent.mig.tmq.weak.WeakTMQ;
import com.tencent.mig.tmq.weak.WeakTmqMsg;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Weak消息的测试，Weak消息体以Date类型模拟
 */
public class WeakMessageTest extends BaseTest {
    // check监听后，待进一步分析的消息暂存于此
    List tempList;

    @Before
    public void setUp()
    {
        TMQ.init(new WeakTMQ());
        TMQ.reset(); // 注意用例里的其他线程要及时关闭，否则连续执行可能会影响下一个用例的结果
        TMQ.switchExpectMode(ModeEnum.STRICT);
        tempList = new ArrayList();
    }

    @After
    public void tearDown()
    {
        TMQ.setCheckListener(null);
    }

    /**
     * 预期收到1条指定消息，实际发出符合条件的1条消息，TMQ校验通过。
     */
    @Test
    public void testStrictOneMessage() throws Exception {
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest", new Date()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    /**
     * 预期收到n条指定消息，实际发出符合条件的n条消息且顺序与预期一致，TMQ校验通过。
     */
    @Test
    public void testStrictMoreMessage() throws Exception {
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest", new Date()),
                new WeakTmqMsg("UnitTest", new Date())
                , new WeakTmqMsg("UnitTest2", new Date())
                , new WeakTmqMsg("UnitTest2", new Date()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest2", new Date());
                TMQ.report("UnitTest2", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    /**
     * 预期消息只填null，语义为预期不会收到任何消息，如有收到任意条消息，TMQ校验不过；
     * 预期消息只填SimpleTmqMsg.NULL，语义与只填null一致，如有收到任意条消息，TMQ校验不过；
     * 预期消息只填SimpleTmqMsg.NULL，如未收到任何消息，TMQ校验通过；
     * 预期消息只填SimpleTmqMsg.KEY_MATCHED_NULL，如未收到任何消息，TMQ校验通过；
     */
    @Test
    public void testStrictZeroMessageExclusive() throws Exception {
        // 预期收不到任何消息
        TMQ.iCareWhatMsg(null);
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest2", new Date());
                TMQ.report("UnitTest2", new Date());
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
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest2", new Date());
                TMQ.report("UnitTest2", new Date());
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

    /**
     * 预期收到n条指定消息，实际收到1条不符合预期的消息，TMQ校验不过；
     * 预期收到n条指定消息，实际收到的消息顺序与预期不符，TMQ校验不过；
     */
    @Test
    public void testStrictMoreMessageExclusive() throws Exception {
        // 预期收不到任何非预期消息
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest", new Date()),
                new WeakTmqMsg("UnitTest", new Date())
                , new WeakTmqMsg("UnitTest2", new Date())
                , new WeakTmqMsg("UnitTest2", new Date()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest2", new Date());
                // 第4个消息不在预期序列，只依赖tag进行判断
                TMQ.report("UnitTest3", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());

        // 收到的消息顺序与预期不符
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest", new Date()),
                new WeakTmqMsg("UnitTest", new Date())
                , new WeakTmqMsg("UnitTest2", new Date())
                , new WeakTmqMsg("UnitTest2", new Date()));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                // 第3、4个消息和预期不一致
                TMQ.report("UnitTest3", new Date());
                TMQ.report("UnitTest4", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());
    }

    /**
     * 设置只关注的消息类型后，收到的其他类型消息不再作为判定依据
     * @throws Exception
     */
    @Test
    public void testStrictMoreMessageFilter() throws Exception {
        // 预期收不到任何非预期消息
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest", new Date()),
                new WeakTmqMsg("UnitTest", new Date())
                , new WeakTmqMsg("UnitTest2", new Date())
                , new WeakTmqMsg("UnitTest2", new Date()));

        // 只关注"UnitTest", "UnitTest2"消息，对于其他的消息不关注，出现也不作为判断依据
        TMQ.iCareWhatType("UnitTest", "UnitTest2");

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest", new Date());
                TMQ.report("UnitTest2", new Date());
                // 不关注的消息
                TMQ.report("UnitTest3", new Date());
                TMQ.report("UnitTest2", new Date());
                // 不关注的消息
                TMQ.report("UnitTest3", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    /**
     * 非测试状态，单纯的TMQ.report应不占用多余内存。
     */
    @Test
    public void testNoTestThread() throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(1);

        TMQ.setCheckListener(new CheckListener<WeakTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<WeakTmqMsg> msgPreFilter, List<WeakTmqMsg> msgAfterFilter, List<WeakTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                assertEquals(msgPreFilter.size(), 0);
                assertEquals(msgAfterFilter.size(), 0);
                assertEquals(msgChecked.size(), 0);
            }
        });
        assertTrue(TMQ.check());
    }

    /**
     * 测试生命周期内的TMQ.report消息才有效。
     */
    @Test
    public void testCycleLife() throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                String tag = "UnitTest";
                int i = 0;
                for (; i < 10; i++)
                {
                    TMQ.report(tag + i, new Date());
                }
                // 隔3s
                try {
                    TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
                    i++; // 11
                    TMQ.report(tag + i, new Date());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ASYNC_TASK_TIMEOUT);

        // 3s后开始测试生命周期
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        TMQ.iCareWhatMsg(new WeakTmqMsg("UnitTest11", new Date()));
        TMQ.await(AWAIT_TIMEOUT);

        TMQ.setCheckListener(new CheckListener<WeakTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<WeakTmqMsg> msgPreFilter, List<WeakTmqMsg> msgAfterFilter, List<WeakTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                assertEquals(msgPreFilter.size(), 1);
                assertEquals(msgPreFilter.get(0).getTag(), "UnitTest11");
            }
        });
        assertTrue(TMQ.check());
    }

    /**
     * 模拟测试验证网络请求消息的可能场景，固定收1条请求消息和1条响应消息。
     */
    @Test
    public void testBinaryMessage() throws Exception {
        // 固定收2条消息，一条代表poi搜索的请求，一条代表poi搜索的响应
        TMQ.iCareWhatMsg(new WeakTmqMsg("JceMessage_poi_req", new Date())
                , new WeakTmqMsg("JceMessage_poi_res", new Date()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                // 模拟被测代码，上报代表请求的消息
                TMQ.report("JceMessage_poi_req", new Date());
                // 模拟被测代码，上报代表响应的消息
                TMQ.report("JceMessage_poi_res", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);

        TMQ.setCheckListener(new CheckListener<WeakTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<WeakTmqMsg> msgPreFilter, List<WeakTmqMsg> msgAfterFilter, List<WeakTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                tempList.addAll(msgChecked);
            }
        });
        // 基本的收全了req和res消息的校验
        assertTrue(TMQ.check());
        // 可以从临时列表中取出待校验消息进行进一步校验了
        assertEquals(tempList.get(0), new WeakTmqMsg("JceMessage_poi_req", new Date()));
        assertEquals(tempList.get(1), new WeakTmqMsg("JceMessage_poi_res", new Date()));
        Object data1 = ((WeakTmqMsg)tempList.get(0)).getMsg();
        assertTrue(data1 instanceof Date);
    }

    /**
     * 模拟测试验证网络请求消息的可能场景，固定收指定类型的1条请求消息和1条响应消息。
     * 注意目前是严格模式，关注的消息的时序也要求与预期一致
     */
    @Test
    public void testTypeBinaryMessage() throws Exception {
        // 固定收2条消息，一条代表poi搜索的请求，一条代表poi搜索的响应
        TMQ.iCareWhatMsg(new WeakTmqMsg("JceMessage_poi_req", new Date())
                , new WeakTmqMsg("JceMessage_poi_res", new Date()));
        // 只关注"JceMessage_poi_req", "JceMessage_poi_res"消息，其他的消息不关注
        TMQ.iCareWhatType("JceMessage_poi_req", "JceMessage_poi_res");
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                // 模拟被测代码，上报代表请求的消息
                TMQ.report("JceMessage_poi_req", new Date());
                // 模拟其他不需要关注的消息
                TMQ.report("OtherMsg", new Date());
                TMQ.report("OtherMsg1", new Date());
                TMQ.report("OtherMsg2", new Date());
                TMQ.report("OtherMsg3", new Date());
                // 模拟被测代码，上报代表响应的消息
                TMQ.report("JceMessage_poi_res", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);

        TMQ.setCheckListener(new CheckListener<WeakTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<WeakTmqMsg> msgPreFilter, List<WeakTmqMsg> msgAfterFilter, List<WeakTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                assertTrue(msgPreFilter.size() == 6);
                assertTrue(msgAfterFilter.size() == 2);
                assertTrue(msgChecked.size() == 2);
                tempList.addAll(msgChecked);
            }
        });
        // 收全了预期的req和res消息的校验
        assertTrue(TMQ.check());
        // 可以从临时列表中取出待校验消息进行进一步校验了
        assertEquals(tempList.get(0), new WeakTmqMsg("JceMessage_poi_req", new Date()));
        assertEquals(tempList.get(1), new WeakTmqMsg("JceMessage_poi_res", new Date()));
    }

    /**
     * 模拟测试验证网络请求消息的可能场景，一段时间内收指定类型的n条请求消息和n条响应消息。
     * 需要切换到松散模式实现，松散模式其实是只有预期消息没有全部收到指定条数时才会校验不过。
     */
    @Test
    public void testFlexibleBinaryMessage() throws Exception {
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        // 至少收2条消息1条代表poi搜索的请求，1条代表poi搜索的响应
        TMQ.iCareWhatMsg(new WeakTmqMsg("JceMessage_poi_req", new Date())
                , new WeakTmqMsg("JceMessage_poi_res", new Date()));
        // 只关注"JceMessage_poi_req", "JceMessage_poi_res"消息，其他的消息不关注
        TMQ.iCareWhatType("JceMessage_poi_req", "JceMessage_poi_res");
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                // 模拟被测代码，上报代表请求的消息
                TMQ.report("JceMessage_poi_req", new Date());
                // 模拟被测代码，上报代表响应的消息
                TMQ.report("JceMessage_poi_res", new Date());
                // 模拟其他不需要关注的消息
                TMQ.report("OtherMsg", new Date());
                TMQ.report("OtherMsg1", new Date());
                TMQ.report("OtherMsg2", new Date());
                TMQ.report("OtherMsg3", new Date());

                try {
                    TimeUnit.SECONDS.sleep(WAIT_TIMEOUT - 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 多个符合条件的请求的消息
                TMQ.report("JceMessage_poi_req", new Date());
                TMQ.report("JceMessage_poi_req", new Date());
                TMQ.report("JceMessage_poi_req", new Date());
                // 多个符合条件的响应的消息
                TMQ.report("JceMessage_poi_res", new Date());
                TMQ.report("JceMessage_poi_res", new Date());
                TMQ.report("JceMessage_poi_res", new Date());
            }
        }, ASYNC_TASK_TIMEOUT);
        // 设置未收到预期消息的超时时间3s，收到预期消息再等3s收多个符合条件的消息
        TMQ.awaitAndSleep(AWAIT_TIMEOUT, WAIT_TIMEOUT);

        TMQ.setCheckListener(new CheckListener<WeakTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<WeakTmqMsg> msgPreFilter, List<WeakTmqMsg> msgAfterFilter, List<WeakTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                assertTrue(msgPreFilter.size() == 12);
                assertTrue(msgAfterFilter.size() == 8);
                assertTrue(msgChecked.size() == 8);
                tempList.addAll(msgChecked);
            }
        });
        // 基本的收全了req和res消息的校验
        assertTrue(TMQ.check());
        // 可以从临时列表中取出待校验消息进行进一步校验了
        assertEquals(tempList.size(), 8);
    }
}