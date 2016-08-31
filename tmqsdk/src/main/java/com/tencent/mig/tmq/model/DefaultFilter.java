package com.tencent.mig.tmq.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 默认的消息过滤器实现
 * @author yoyoqin, kaihuancao
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public abstract class DefaultFilter<T, M> implements IFilter<T, M> {
	// 可以预先设置消息按类型过滤
	protected Set<T> filterSet = new HashSet<>();

	@Override
	public void willFilter(T caredType) {
		if (caredType == null) {
			return;
		}

		filterSet.add(caredType);
	}

	@Override
	public void clear() {
		filterSet.clear();
	}
}
