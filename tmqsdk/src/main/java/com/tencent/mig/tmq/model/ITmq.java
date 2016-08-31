package com.tencent.mig.tmq.model;

import java.io.OutputStream;

/**
 * TMQ对外接口
 * @author yoyoqin, kaihuancao
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface ITmq<T, M> {
    /**
     * 设置执行控制器
     * @param controller 控制器
     * @return ITmq对象
     */
    ITmq setExecuteController(IExecuteControllers<T, M> controller);

    /**
     * 切换过滤器
     * @param filter 过滤器
     * @return ITmq对象
     */
    ITmq switchFilter(IFilters<T, M> filter);

    /**
     * 切换匹配模式
     * @param mode 匹配模式
     * @return ITmq对象
     */
    ITmq switchExpectMode(IExpectModes<T, M> mode);

    /**
     * 被测代码中调用report方法，将消息发给TMQ测试框架
     * @param tag 消息标签
     * @param msg 消息内容，TMQ框架不持有msg对象，不会造成内存泄露
     * @return 消息上报成功，目前全部返回true
     */
    boolean report(T tag, Object msg);

    /**
     * 预设有效的消息序列，与匹配模式配合进行消息的校验
     * @param tmqMsgList 有效的消息序列
     */
    void iCareWhatMsg(M... tmqMsgList);

    /**
     * 预设有效的消息标签列表，与过滤器配合进行初步的消息过滤
     * @param msgTypes 有效的消息标签序列
     */
    void iCareWhatType(T... msgTypes);

    /**
     * 清理已在过滤器中设置消息标签
     */
    void clearCaredType();

    /**
     * 重置TMQ框架状态，会将预设的关注消息序列、预设的有效消息标签和当前用例已记录的消息列表都清空
     */
    void reset();

    /**
     * 等待被测系统发给TMQ的消息通过匹配器的检查
     * @return 成功通过匹配器检查 true，到10s还没通过 false
     */
    boolean await();

    /**
     * 等待被测系统发给TMQ的消息通过匹配器的检查
     * @param timeout 超时时间
     * @return 成功通过匹配器检查 true，到设置的超时实践还没通过 false
     */
    boolean await(long timeout);

    /**
     * 在完成等待接收消息流程后，检查收到的消息是否与预期匹配
     * @return 匹配 true, 不匹配 false
     */
    boolean check();

    /**
     * 设置已记录消息的输出流
     * @param os 输出流
     */
    void setOutStream(OutputStream os);

    /**
     * 向已设置的输出流打印用例执行过程中记录的消息情况
     * @param head 打印前输出的文本行，如果不需要可以是null
     * @param foot 打印后输出的文本行，如果不需要可以是null
     */
    void printHistory(String head, String foot);

    /**
     * 向已设置的输出流打印一行文本
     * @param head 打印前输出的文本行，如果不需要可以是null
     * @param foot 打印后输出的文本行，如果不需要可以是null
     * @param text 打印的文本行
     */
    void printText(String head, String foot, String text);
}
