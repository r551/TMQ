package com.tencent.mig.tmq.model;

/**
 * 匹配模式
 * @author yoyoqin, kaihuancao
 *
 * @param <M> 匹配的消息类型
 */
public interface IExpectMode<T, M> {
	/**
	 * 预设关注的消息
	 * @param msg
	 */
	void willCare(M msg);
	
	/**
	 * 对输入的消息进行匹配
	 * @param msg 要匹配的消息
	 * @return 匹配 true; 不匹配 false
	 */
	boolean match(M msg);
	
	/**
	 * 检查记录中的消息，是否符合本预期模式的预期
	 * @return 返回成功 0, 其他 各种失败情况
	 */
	IRetCode check(ILogger<T, M> logger);
}
