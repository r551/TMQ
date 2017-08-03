package com.tencent.mig.tmq;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

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
        TMQ.setOutStream(System.out);
        TMQ.printText(testName.getMethodName() + " start..");
    }

    @After
    public void tearDown()
    {
        TMQ.setOutStream(System.out);
        TMQ.printText(testName.getMethodName() + " end.");
    }
}
