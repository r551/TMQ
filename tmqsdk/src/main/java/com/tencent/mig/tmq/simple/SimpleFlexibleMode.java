package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.DefaultFlexibleMode;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yoyoqin on 2016/9/28.
 */
public class SimpleFlexibleMode<T, M> extends DefaultFlexibleMode<T, M> {
    @Override
    public IRetCode check(ILogger<T, M> logger) {
        for (Map.Entry<M, AtomicInteger> entry : expectedMap.entrySet())
        {
            AtomicInteger atomicInteger = entry.getValue();

            // 只要有一个预期的消息计算器没到0，就说明消息没收全
            if (atomicInteger.get() > 0 && ! entry.getKey().equals(SimpleTmqMsg.NULL))
            {
                return RetCode.NOT_RECEIVED_ALL_EXPECTED_QUEUE_MESSAGE;
            }
        }

        List<M> afterFilterQueue = logger.getAfterFilterQueue();
        // 在关键消息验证完成后，不应该再收到非关键消息类型的其他消息了
//        for (int i = indexWhenCompleteKeyMatch; i < afterFilterQueue.size(); i++)
        // 如果收到其他消息，即判定为不通过
        for (int i = 0; i < afterFilterQueue.size(); i++)
        {
            M m = afterFilterQueue.get(i);
            if (expectedMap.get(m) == null)
            {
//                return RetCode.RECEIVED_OTHER_MESSAGES_AFTER_EXCLUSIVE_EXPECT_MESSAGE;
                return RetCode.RECEIVED_OTHER_MESSAGES_EXCLUSIVE_EXPECT_MESSAGE;
            }
        }

        return RetCode.SUCCESS;
    }
}
