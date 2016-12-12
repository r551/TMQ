package com.tencent.mig.tmq;

import com.tencent.mig.tmq.model.CheckListener;
import com.tencent.mig.tmq.model.IRetCode;
import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.RetCode;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;

import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created by yoyoqin on 2016/12/5.
 */
public class CheckListenerTest extends BaseTest {
    /**
     * 验证check成功时的监听
     * @throws Exception
     */
    @Test
    public void testCheckListenerSuccess() throws Exception {
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await();
        BlockingQueue<IRetCode> queue = new LinkedBlockingQueue();
        TMQ.setCheckListener(new CheckListener<SimpleTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<SimpleTmqMsg> msgPreFilter, List<SimpleTmqMsg> msgAfterFilter, List<SimpleTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                if (retCode == RetCode.SUCCESS)
                {
                    queue.offer(retCode);
                }
                TMQ.printText(msgGroupArray[0]);
                TMQ.printText(msgGroupArray[1]);
                TMQ.printText(msgGroupArray[2]);
            }
        });
        assertTrue(TMQ.check());
        IRetCode retCode = queue.poll();
        assertTrue(retCode != null);
        TMQ.setCheckListener(null);
    }

    /**
     * 验证check失败时的监听
     * @throws Exception
     */
    @Test
    public void testCheckListenerFail() throws Exception {
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await();
        BlockingQueue<IRetCode> queue = new LinkedBlockingQueue();
        TMQ.setCheckListener(new CheckListener<SimpleTmqMsg>() {
            @Override
            public void onCheck(IRetCode retCode,
                                List<SimpleTmqMsg> msgPreFilter, List<SimpleTmqMsg> msgAfterFilter, List<SimpleTmqMsg> msgChecked,
                                String[] msgGroupArray) {
                if (retCode == RetCode.SUCCESS)
                {
                    queue.offer(retCode);
                }
                else
                {
                    queue.offer(retCode);
                }
                TMQ.printText(msgGroupArray[0]);
                TMQ.printText(msgGroupArray[1]);
                TMQ.printText(msgGroupArray[2]);
            }
        });
        assertTrue(!TMQ.check());
        IRetCode retCode = queue.poll();
        assertTrue(retCode != null);
        TMQ.setCheckListener(null);
    }
}
