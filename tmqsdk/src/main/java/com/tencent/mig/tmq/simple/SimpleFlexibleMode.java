package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.DefaultFlexibleMode;
import com.tencent.mig.tmq.model.IExclusiveFlag;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yoyoqin on 2016/9/28.
 */
public class SimpleFlexibleMode<T, M> extends DefaultFlexibleMode<T, M> {
    @Override
    public boolean check(M msg) {
        return expectedMap.get(msg) == null ? false : true;
    }

    @Override
    public IRetCode check(ILogger<T, M> logger) {
        // 如果有排他标志，暂存后用
        IExclusiveFlag exclusiveFlag = null;
        for (Map.Entry<M, AtomicInteger> entry : expectedMap.entrySet())
        {
            AtomicInteger atomicInteger = entry.getValue();

            // 只要有一个预期的消息计算器没到0（除了排他类消息），就说明消息没收全
            if (atomicInteger.get() > 0 && !(entry.getKey() instanceof IExclusiveFlag))
            {
                return RetCode.NOT_RECEIVED_ALL_EXPECTED_QUEUE_MESSAGE;
            }

            if (entry.getKey() instanceof IExclusiveFlag)
            {
                exclusiveFlag = (IExclusiveFlag)entry.getKey();
            }
        }

        // 下面这部分为排他检查
        if (exclusiveFlag != null)
        {
            return exclusiveFlag.exclusiveCheck(this, logger);
        }

        return RetCode.SUCCESS;
    }
}
