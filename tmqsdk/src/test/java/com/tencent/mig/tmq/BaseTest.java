package com.tencent.mig.tmq;

import org.junit.After;
import org.junit.Before;

/**
 * 测试父类
 */
public class BaseTest {
    public static int ASYNC_TASK_TIMEOUT = 1000; // milliseconds
    public static int AWAIT_TIMEOUT = 3; // seconds
    public static int WAIT_TIMEOUT = 3; // seconds

    @Before
    public void setUp()
    {
        TMQ.setOutStream(System.out);
        TMQ.printText("start..");
    }

    @After
    public void tearDown()
    {
        TMQ.setOutStream(System.out);
        TMQ.printText("end.");
    }
}
