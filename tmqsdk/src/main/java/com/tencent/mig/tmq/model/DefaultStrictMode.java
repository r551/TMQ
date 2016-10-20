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
package com.tencent.mig.tmq.model;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 默认的严格匹配模式，其语义是预设匹配消息后，在接收消息期间收到的所有消息必须与预设的消息一一对应
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public abstract class DefaultStrictMode<T, M> implements IExpectMode<T, M> {
	// 预期消息序列
	protected Queue<M> expectedQueue = new LinkedList<>();

	@Override
	public void willCare(M msg) {
		expectedQueue.add(msg);
	}

	@Override
	public void clear() {
		expectedQueue.clear();
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

	@Override
	public boolean keyMatched(M msg) {
		return true;
	}

	public int getCountWhenCompleteKeyMatch() {
		return expectedQueue.size();
	}
}
