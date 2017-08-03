package com.tencent.mig.tmq;

import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.SimpleTMQ;
import com.tencent.mig.tmq.weak.WeakTMQ;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.ArrayList;

/**
 * 测试父类
 */
public class BaseTest {
    public static int ASYNC_TASK_TIMEOUT = 100; // milliseconds
    public static int AWAIT_TIMEOUT = 3; // seconds
    public static int WAIT_TIMEOUT = 3; // seconds

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp()
    {
        TMQ.init(new SimpleTMQ());
        TMQ.reset(); // 注意用例里的其他线程要及时关闭，否则连续执行可能会影响下一个用例的结果
        TMQ.switchExpectMode(ModeEnum.STRICT);

        TMQ.setOutStream(System.out);
        TMQ.printText(testName.getMethodName() + " start..");
    }

    @After
    public void tearDown()
    {
        TMQ.setCheckListener(null);

        TMQ.setOutStream(System.out);
        TMQ.printText(testName.getMethodName() + " end.");
    }
}
