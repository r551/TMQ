# TMQ
#### Headers
TMQ是用于测试进度控制的组件，主要用于在被测代码中埋点，输出能够标识被测系统关键状态的信息；在测试代码中接收到被测代码输出的关键状态信息，通过不同的检查策略的切换，灵活的判断测试用例是否执行通过。  
TMQ是从腾讯MIG移动端测试经验中总结出的简洁、有效、高可扩展性的通用组件，广泛的用于增强了检查点的 Android UI自动化项目。
#### 当前版本
v0.2.2
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
    debugCompile 'com.tencent.mig.tmq:tmqsdk:0.2.2'
    releaseProvided  'com.tencent.mig.tmq:tmqsdk:0.2.2'
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

