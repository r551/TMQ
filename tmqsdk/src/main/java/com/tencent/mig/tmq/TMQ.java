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

    public static boolean check()
    {
        return tmqInstance.check();
    }

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
     * 有效的消息序列初始化
     * @param msgTypes 有效的消息类型
     */
    public static <T> void iCareWhatType(T...msgTypes) {
        tmqInstance.iCareWhatType(msgTypes);
    }

    public static boolean await() {
        return tmqInstance.await();
    }

    public static boolean await(long timeout) {
        return tmqInstance.await(timeout);
    }

    public static void printHistory()
    {
        tmqInstance.printHistory(mHead, mFoot);
    }

    public static void printText(String text)
    {
        tmqInstance.printText(mHead, mFoot, text);
    }



    public static void setHeadAndFoot(String head, String foot)
    {
        mHead = head;
        mFoot = foot;
    }

    public static void setOutStream(OutputStream os)
    {
        tmqInstance.setOutStream(os);
    }
}
