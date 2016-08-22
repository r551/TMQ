package com.tencent.mig.tmq.model;

import java.util.LinkedList;
import java.util.Queue;

public abstract class DefaultStrictMode<T, M> implements IExpectMode<T, M> {
	// 预期消息序列
	protected Queue<M> expectedQueue = new LinkedList<>();

	@Override
	public void willCare(M msg) {
		expectedQueue.add(msg);
	}

	@Override
	public boolean match(M msg) {
		boolean res = false;
		if (msg == null) {
			return res;
		}
		res = expectedQueue.peek().equals(msg) ? true : false;
		if (res)
		{
			// 匹配后，要出队，这样下一次才会匹配下条消息
			expectedQueue.poll();
		}
		return res;
	}
}
