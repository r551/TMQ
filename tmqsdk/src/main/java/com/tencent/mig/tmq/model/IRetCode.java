package com.tencent.mig.tmq.model;

/**
 * 结果接口，在IExpectMode.check(ILogger<T, M> logger)方法中作为返回结果
 * @author yoyoqin
 */
public interface IRetCode {
	String getDesc();
}
