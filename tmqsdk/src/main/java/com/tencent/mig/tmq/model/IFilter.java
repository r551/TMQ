package com.tencent.mig.tmq.model;

/**
 * 消息过滤器
 * @author yoyoqin, kaihuancao
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface IFilter<T, M> {
	/**
	 * 添加关注的消息标识，会在本过滤器中记录
	 * @param cared 所关注的消息标识，可以是消息本身，也可以是消息的tag之类的，接口中不做约束
	 */
	void willFilter(T cared);
	
	/**
	 * 校验消息
	 * @param msg 被校验的消息体
	 * @return 校验通过 true, 校验失败 false
	 */
	boolean validate(M msg);

	/**
	 * 清理本过滤器的记录
	 */
	void clear();
}
