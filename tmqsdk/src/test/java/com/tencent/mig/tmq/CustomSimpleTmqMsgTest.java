package com.tencent.mig.tmq;

import com.tencent.mig.tmq.model.CheckListener;
import com.tencent.mig.tmq.model.IRetCode;
import com.tencent.mig.tmq.simple.RetCode;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;

import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertTrue;

/**
 * Created by yoyoqin on 2016/12/5.
 * 自定义SimpleTmqMsg测试集
 */
public class CustomSimpleTmqMsgTest extends BaseTest {
    /**
     * 自定义SimpleTmqMsg测试
     * @throws Exception
     */
    @Test
    public void testCheckListenerSuccess() throws Exception {
        CustomSimpleTmqMsg expectMsg = new CustomSimpleTmqMsg("UnitTest", "1");
        TMQ.iCareWhatMsg(expectMsg);

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                CustomSimpleTmqMsg sendMsg = new CustomSimpleTmqMsg("UnitTest", "1");
                sendMsg.costTime = 5000;
                TMQ.report("UnitTest", sendMsg);
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await();

        TMQ.setCheckListener(new CheckListener<SimpleTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<SimpleTmqMsg> msgPreFilter, List<SimpleTmqMsg> msgAfterFilter, List<SimpleTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                long costTime = ((CustomSimpleTmqMsg)msgChecked.toArray()[0]).costTime;
                assertTrue(costTime == 5000);
            }
        });
        assertTrue(TMQ.check());
    }

    class CustomSimpleTmqMsg extends SimpleTmqMsg {
        public long costTime = 0;
        public CustomSimpleTmqMsg(String tag, String msg) {
            super(tag, msg);
        }
    }
}
