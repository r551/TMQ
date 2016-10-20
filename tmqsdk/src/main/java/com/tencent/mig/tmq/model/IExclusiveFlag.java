package com.tencent.mig.tmq.model;

import com.tencent.mig.tmq.simple.RetCode;

/**
 * 排他消息标识接口
 * Created by yoyoqin on 2016/10/20.
 */
public interface IExclusiveFlag<T, M> {
    IRetCode exclusiveCheck(IExpectMode<T, M> mode, ILogger<T, M> logger);
}
