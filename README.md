# TMQ
#### Headers
TMQ是用于测试进度控制的组件，主要用于在被测代码中埋点，输出能够标识被测系统关键状态的信息；在测试代码中接收到被测代码输出的关键状态信息，通过不同的检查策略的切换，灵活的判断测试用例是否执行通过。  
TMQ是从腾讯MIG移动端测试经验中总结出的简洁、有效、高可扩展性的通用组件，广泛的用于增强了检查点的 Android UI自动化项目。
#### 当前版本
v0.2.3
#### 模块介绍
##### tmqsdk
TMQ的主框架，默认采用严格校验模式的消息检查方案，使用者可以在不修改框架代码的情况下，新增自定义的消息检查模块，符合开闭原则。
##### tmqsdk_mini_strict
简化的严格校验模式消息检查器，只有一个类文件，可以不依赖于主框架独立使用，但不具可扩展性。
#### 使用方法
##### 配置
被测源码模块的gradle中配置：
```groovy
dependencies {
    /*
     * 只在debug版本中为配合自动化测试使用，
     * 所以这里配置为release版本编译方式为编译依赖但打包不包含的releaseProvided，
     * 为了在release版本编译时不出错，请在被测代码中调用TMQ的地方用if (BuildConfig.DEBUG)语句包裹
     */
    debugCompile 'com.tencent.mig.tmq:tmqsdk:0.2.3'
    releaseProvided  'com.tencent.mig.tmq:tmqsdk:0.2.3'
}
```
##### DEMO
被测代码中使用TMQ的地方:
```java
if (BuildConfig.DEBUG)
{
    audioRecord.setNotificationMarkerPosition(10);
    audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord recorder) {
            /*
             * 因为通过UI自动化中无法判断录音是否有效，这里在被测代码中埋点：
             * 将录音10帧的事件作为判断录音正常启动的标志事件
             */
            TMQ.report("RecordAudio", "audioRecord.onMarkerReached10");
        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {

        }
    });
}
```
测试代码中：
```java
    ...
    ...
    // 首先设置预期正常流程能够收到的消息
    TMQ.iCareWhatMsg(
        new SimpleTmqMsg("RecordAudio", "audioRecord.onStart")
        new SimpleTmqMsg("RecordAudio", "audioRecord.onMarkerReached10"),
        new SimpleTmqMsg("RecordAudio", "audioRecord.onCompleted")
    );
    // do something 这里通过UI自动化驱动录音
    ...
    // 在这里按预设的检查模式检查预期的消息，TMQ会将测试线程阻塞在这里等待新的消息到来加入检查，最多阻塞10s
    TMQ.await(10); 
    // 在这里对前面设置关注的消息进行序列的严格检查
    Assert.assertTrue(TMQ.check());
```
#### 框架扩展方法
请参考tmqsdk工程下的simple包自定义扩展模块。
#### 使用方法详解
```java
public class StrictModeTest extends BaseTest {

    @Test
    public void testStrictOneMessage() throws Exception {
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    @Test
    public void testStrictMoreMessage() throws Exception {
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest2", "1")
                , new SimpleTmqMsg("UnitTest2", "2"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest2", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }

    @Test
    public void testStrictZeroMessageExclusive() throws Exception {
        // 预期收不到任何消息
        TMQ.iCareWhatMsg(null);
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest2", "2");
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
                TMQ.report("UnitTest2", "1");
                TMQ.report("UnitTest2", "2");
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
    public void testStrictMoreMessageExclusive() throws Exception {
        // 预期收不到任何非预期消息
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest2", "1")
                , new SimpleTmqMsg("UnitTest2", "2"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest2", "1");
                // 第4个消息不在预期序列
                TMQ.report("UnitTest2", "3");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertFalse(TMQ.check());

        // 收到的消息顺序与预期不符
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest2", "1")
                , new SimpleTmqMsg("UnitTest2", "2"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                // 第3、4个消息和预期不一致
                TMQ.report("UnitTest2", "2");
                TMQ.report("UnitTest2", "1");
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
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"),
                new SimpleTmqMsg("UnitTest", "2")
                , new SimpleTmqMsg("UnitTest2", "1")
                , new SimpleTmqMsg("UnitTest2", "2"));

        // 只关注"UnitTest", "UnitTest2"消息，对于其他的消息不关注，出现也不作为判断依据
        TMQ.iCareWhatType("UnitTest", "UnitTest2");

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
                TMQ.report("UnitTest", "2");
                TMQ.report("UnitTest2", "1");
                // 不关注的消息
                TMQ.report("UnitTest3", "1");
                TMQ.report("UnitTest2", "2");
                // 不关注的消息
                TMQ.report("UnitTest3", "2");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }
}

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

public class MixedModeTest extends BaseTest {

    @Test
    public void testMixedMessage() throws Exception {
        // 严格模式
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());

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
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());

        // 切换到严格模式
        TMQ.switchExpectMode(ModeEnum.FLEXIBLE);
        TMQ.iCareWhatMsg(new SimpleTmqMsg("UnitTest", "1"));
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                TMQ.report("UnitTest", "1");
            }
        }, ASYNC_TASK_TIMEOUT);
        TMQ.await(AWAIT_TIMEOUT);
        assertTrue(TMQ.check());

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
        TMQ.await(AWAIT_TIMEOUT);
        TimeUnit.SECONDS.sleep(WAIT_TIMEOUT);
        assertTrue(TMQ.check());
    }
}
```