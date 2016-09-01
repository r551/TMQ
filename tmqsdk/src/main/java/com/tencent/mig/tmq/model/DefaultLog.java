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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 默认的消息记录器实现
 * @author yoyoqin, kaihuancao
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public class DefaultLog<T, M> implements ILogger<T, M> {
	static int CAPACITY = 256;

	IFilter<T, M> filter;

	// 过滤前的实际消息序列，要有上限，避免一直不校验撑爆内存
	protected List<M> preFilterMsgQueue = new LinkedList<>();

	// 过滤后的实际消息序列，要有上限，避免一直不校验撑爆内存
	protected List<M> msgQueue = new ArrayList<>();

	// 通过校验的消息序列
	protected Queue<M> checkedQueue = new LinkedList<>();

	@Override
	public boolean append(M msg) {
		if (preFilterMsgQueue.size() < CAPACITY * 2)
			preFilterMsgQueue.add(msg);

		if (filter.validate(msg)) {
			if (msgQueue.size() < CAPACITY) {
				msgQueue.add(msg);
			}
		}
		else
		{
			return false;
		}

		return true;
	}

	@Override
	public void appendCheckedMsg(M msg) {
		checkedQueue.add(msg);
	}

	@Override
	public void clear() {
		preFilterMsgQueue.clear();
		msgQueue.clear();
		checkedQueue.clear();
	}

	/**
	 * 默认的日志过程输出,
	 * 分三段输出，分别是过滤前的完整消息序列，通过过滤的消息序列，通过校验的消息序列
	 */
	@Override
	public String[] getHistory()
	{
		String[] result = new String[3];
		StringBuilder preFilterMsgs = new StringBuilder();
		for (M s : preFilterMsgQueue)
		{
			preFilterMsgs.append(s);
			preFilterMsgs.append(System.getProperty("line.separator"));
		}
		result[0] = preFilterMsgs.toString();
		
		StringBuilder msgs = new StringBuilder();
		for (M s : msgQueue)
		{
			msgs.append(s);
			preFilterMsgs.append(System.getProperty("line.separator"));
		}
		result[1] = msgs.toString();
		
		StringBuilder checkedMsgs = new StringBuilder();
		for (M s : checkedQueue)
		{
			checkedMsgs.append(s);
			preFilterMsgs.append(System.getProperty("line.separator"));
		}
		result[2] = checkedMsgs.toString();
		
		return result;
	}

	@Override
	public void setFilter(IFilter<T, M> filter) {
		this.filter = filter;
	}

	@Override
	public List<M> getPreFilterQueue() {
		return preFilterMsgQueue;
	}

	@Override
	public List<M> getAfterFilterQueue() {
		return msgQueue;
	}

	@Override
	public Queue<M> getCheckedQueue() {
		return checkedQueue;
	}
}
