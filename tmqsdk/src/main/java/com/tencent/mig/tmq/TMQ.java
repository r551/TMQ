/*
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.mig.tmq;

import com.tencent.mig.tmq.model.IExpectModes;
import com.tencent.mig.tmq.model.IFilters;
import com.tencent.mig.tmq.model.ITmq;
import com.tencent.mig.tmq.simple.SimpleTMQ;

import java.io.OutputStream;

public class TMQ {
    // 默认的TMQ引擎使用SimpleTMQ类型
    private static ITmq tmqInstance = new SimpleTMQ();
    private static String mHead, mFoot;

    // 设置新的TMQ引擎
    public static void init(ITmq tmq)
    {
        tmqInstance = tmq;
    }

    public static <T, M> boolean report(T tag, M msg) {
       return tmqInstance.report(tag.toString(), msg);
    }

    /**
     * 切换过滤器
     * @param filter 过滤器
     */
    public static <T, M> void switchFilter(IFilters<T, M> filter)
    {
        tmqInstance.switchFilter(filter);
    }

    /**
     * 切换匹配模式
     * @param mode 匹配模式
     * @return ITmq对象
     */
    public static <T, M> void switchExpectMode(IExpectModes<T, M> mode)
    {
        tmqInstance.switchExpectMode(mode);
    }

    /**
     * 在完成等待接收消息流程后，检查收到的消息是否与预期匹配
     * @return 匹配 true, 不匹配 false
     */
    public static boolean check()
    {
        return tmqInstance.check();
    }

    /**
     * 重置TMQ框架状态，会将预设的关注消息序列、预设的有效消息标签和当前用例已记录的消息列表都清空
     */
    public static void reset() {
        tmqInstance.reset();
    }

    /**
     * 有效的消息序列初始化
     * @param msgList 有效的消息序列
     */
    public static <M> void iCareWhatMsg(M...msgList) {
        tmqInstance.iCareWhatMsg(msgList);
    }

    public static void clearCaredType()
    {
        tmqInstance.clearCaredType();
    }

    /**
     * 有效的消息类型序列初始化
     * @param msgTypes 有效的消息类型
     */
    public static <T> void iCareWhatType(T...msgTypes) {
        tmqInstance.iCareWhatType(msgTypes);
    }

    /**
     * 等待被测系统发给TMQ的消息通过匹配器的检查
     * @return 成功通过匹配器检查 true，到10s还没通过 false
     */
    public static boolean await() {
        return tmqInstance.await();
    }

    /**
     * 等待被测系统发给TMQ的消息通过匹配器的检查
     * @param timeout 超时时间
     * @return 成功通过匹配器检查 true，到设置的超时实践还没通过 false
     */
    public static boolean await(long timeout) {
        return tmqInstance.await(timeout);
    }

    /**
     * await通过后再等1s，继续收可能出现的预期之外的消息
     * @return 成功通过匹配器检查 true，到超时时间还没通过 false
     */
    public static boolean awaitAndSleep() {
        return tmqInstance.awaitAndSleep();
    }

    /**
     * 等待被测系统发给TMQ的消息通过匹配器的检查，若通过检查则再睡眠指定时长
     * @param timeout 超时时间，单位秒
     * @param delay await后的睡眠时长，单位秒
     * @return 成功通过匹配器检查 true，到超时时间还没通过 false
     */
    public static boolean awaitAndSleep(long timeout, long delay) {
        return tmqInstance.awaitAndSleep(timeout, delay);
    }

    /**
     * 打印历史消息记录
     */
    public static void printHistory()
    {
        tmqInstance.printHistory(mHead, mFoot);
    }

    /**
     * 打印单行文本
     */
    public static void printText(String text)
    {
        tmqInstance.printText(mHead, mFoot, text);
    }

    /**
     * 设置打印的头部信息和尾部信息
     */
    public static void setHeadAndFoot(String head, String foot)
    {
        mHead = head;
        mFoot = foot;
    }

    /**
     * 设置打印信息的输出流
     * @param os 输出流
     */
    public static void setOutStream(OutputStream os)
    {
        tmqInstance.setOutStream(os);
    }

    //=========================================================================
    /**
     * 设置临时消息，方便异步生成的需要在用例层使用的对象。
     * 临时消息非用于结果验证的消息。
     * 在设置临时消息后，如果未调用pollTempMsg收取该消息，则无法设置新的临时消息
     * @param o
     */
    public static void offerTempMsg(Object o) {
        tmqInstance.offerTempMsg(o);
    }

    /**
     * 获取最近发出的异步临时消息，与offerTempMsg配套使用
     * @return 异步设置的临时消息,如超过默认的超时时间10s，则返回null
     */
    public static Object pollTempMsg() {
        return tmqInstance.pollTempMsg(10000);
    }

    /**
     * 获取最近发出的异步临时消息，与offerTempMsg配套使用
     * @param timeout 超时时间，单位秒
     * @return 异步设置的临时消息,如超过设定的超时时间，则返回null
     */
    public static Object pollTempMsg(int timeout) {
        return tmqInstance.pollTempMsg(timeout);
    }

    /**
     * 清理临时消息。
     *
     * 如果用例执行fail，队列中有消息未被消化，
     * 会导致后面用例pollTempMsg瞬间拉到前一个用例未被消耗的消息，不符合预期，
     * 所以这里提供clear方法，稳妥起见建议用户在@After中调用该方法清理一下。
     */
    public static void clearTempMsg() {
        tmqInstance.clearTempMsg();
    }
}
