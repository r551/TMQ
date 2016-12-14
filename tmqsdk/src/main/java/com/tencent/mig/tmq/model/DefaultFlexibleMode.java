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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最松散灵活匹配模式，其语义是支持：
 * 1.按消息收（按类型需要重新扩展，不限定个数)
 * 2.不按消息顺序进行校验
 * 3.消息不能再是简单消息，分类还应该是分类
 * 4.即时收消息的逻辑需要注意匹配规则
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public abstract class DefaultFlexibleMode<T, M> implements IExpectMode<T, M> {

	// 预期消息序列
	protected Map<M, AtomicInteger> expectedMap = new HashMap();
	// 在匹配到排他预期之前，还应该收到多少条关键关注消息，用于收完整关键消息后再启动排他的match模式
	protected int needValidBeforeNullMsg = 0;
	// 关键消息都匹配后，此时收到的过滤后消息个数
	protected int countWhenCompleteKeyMatch = 0;
	protected boolean expectNullFlag = false;

	/**
	 * // 如果加入NULL消息，意味着除关心的消息外，其他类型的消息都不应该出现
	 * @param msg 加入的消息
     */
	@Override
	public void willCare(M msg) {
		AtomicInteger count = null;
		if (!expectedMap.containsKey(msg))
		{
			count = new AtomicInteger(0);
			expectedMap.put(msg, count);
		}
		else
		{
			count = expectedMap.get(msg);
		}
		if (msg instanceof IExclusiveFlag)
		{
			expectNullFlag = true;
		}
		if (! expectNullFlag)
		{
			needValidBeforeNullMsg++;
		}
		count.incrementAndGet();
	}

	@Override
	public void clear() {
		expectNullFlag = false;
		needValidBeforeNullMsg = 0;
		countWhenCompleteKeyMatch = 0;
		expectedMap.clear();
	}

	@Override
	public boolean match(M msg) {
		boolean matched = expectedMap.containsKey(msg);
		// 注意这个判断应该在keyMatched方法中indexWhenCompleteKeyMatch--逻辑之前
		if (matched && needValidBeforeNullMsg > 0)
		{
			countWhenCompleteKeyMatch++;
		}
		return matched;
	}

	/**
	 * 消息对应AtomicInteger减到0之前，都是关键消息
	 * @param msg 要匹配的消息
	 * @return
     */
	@Override
	public boolean keyMatched(M msg) {
		AtomicInteger count = expectedMap.get(msg);
		int countValue = count.decrementAndGet();
		if (countValue >= 0)
		{
			needValidBeforeNullMsg--;
		}
		return countValue >= 0 ? true : false;
	}

	public int getCountWhenCompleteKeyMatch() {
		return countWhenCompleteKeyMatch;
	}

	@Override
	public List<M> getUnHitMsgList()
	{
		ArrayList<M> ret = new ArrayList<M>();
		for (Map.Entry<M, AtomicInteger> entry : expectedMap.entrySet())
		{
			if (entry.getValue().get() > 0)
			{
				ret.add(entry.getKey());
			}
		}
		return ret;
	}
}
