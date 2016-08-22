package com.tencent.mig.tmq.model;

import java.util.List;
import java.util.Queue;

/**
 * 消息记录日志
 * @author yoyoqin, kaihuancao
 *
 * @param <M> 记录的消息类型
 */
public interface ILogger<T, M> {
	/**
	 * 记录一条消息
	 * @param msg 被记录的消息
	 * @return 在使用TMQ模块时，默认为true
	 */
	boolean append(M msg);

	/**
	 * 记录一条消息到已匹配队列
	 * @param msg 被记录的消息
	 */
	void appendCheckedMsg(M msg);

	/**
	 * 获取所有记录的消息队列
	 * @return
	 */
	List<M> getPreFilterQueue();

	/**
	 * 获取过滤后的消息队列
	 * @return
	 */
	List<M> getAfterFilterQueue();

	/**
	 * 获取校验通过的消息队列
	 * @return
	 */
	Queue<M> getCheckedQueue();

	/**
	 * 清空已记录的消息日志
	 */
	void clear();

	/**
	 * 以字符串分段的方式获取消息记录信息
	 * @return 已记录的消息信息
	 */
	String[] getHistory();

	/**
	 * 设置消息过滤器
	 * @param filter
	 */
	void setFilter(IFilter<T, M> filter);
}
