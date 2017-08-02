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
package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.CheckListener;
import com.tencent.mig.tmq.model.IExclusiveFlag;
import com.tencent.mig.tmq.model.IExecuteController;
import com.tencent.mig.tmq.model.IExecuteControllers;
import com.tencent.mig.tmq.model.IExpectModes;
import com.tencent.mig.tmq.model.IFilters;
import com.tencent.mig.tmq.model.ITmq;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SimpleTMQ implements ITmq<String, SimpleTmqMsg>  {

	private IExecuteController<String, SimpleTmqMsg> controller;

	public SimpleTMQ()
	{
		// 默认的设置
		this.setExecuteController(ControllerEnum.SIMPLE)
				.switchFilter(FilterEnum.TYPE)
				.switchExpectMode(ModeEnum.STRICT);
	}

	@Override
	public SimpleTMQ setExecuteController(IExecuteControllers<String, SimpleTmqMsg> controller)
	{
		this.controller = controller.getController();
		return this;
	}

	@Override
	public SimpleTMQ switchFilter(IFilters<String, SimpleTmqMsg> filter) {
		controller.switchFilter(filter);
		return this;
	}

	@Override
	public SimpleTMQ switchExpectMode(IExpectModes<String, SimpleTmqMsg> mode) {
		controller.switchExpectMode(mode);
		return this;
	}

	@Override
	public boolean check(){
		return controller.check();
	}

	@Override
	public boolean report(String tag, Object msg) {
		return controller.report(tag, msg);
	}

	/**
	 * 有效的消息序列初始化
	 * 
	 * @param tmqMsgList
	 *            有效的消息序列
	 */
	@Override
	public void iCareWhatMsg(SimpleTmqMsg... tmqMsgList) {
		// 增加设置null值为预期不应该收到任何消息的逻辑
		if (tmqMsgList == null || tmqMsgList.length == 0
				|| tmqMsgList[0].equals(SimpleTmqMsg.NULL))
		{
			controller.reset(0);
			controller.willCare(null);
			return;
		}

		boolean hasExclusiveMsg = false;
		int indexBeforeExclusiveFlag = 0;
		for (; indexBeforeExclusiveFlag < tmqMsgList.length; indexBeforeExclusiveFlag++)
		{
			SimpleTmqMsg msg = tmqMsgList[indexBeforeExclusiveFlag];
			if (msg == null || msg instanceof IExclusiveFlag)
			{
				hasExclusiveMsg = true;
				break;
			}
		}

		controller.reset(indexBeforeExclusiveFlag);
		for (SimpleTmqMsg msg : tmqMsgList) // 这个地方加倒是可以都加上
		{
			// 单条msg有可能是null，此时语义代表为在null后不应该收到其他通过过滤器的任何消息
			controller.willCare(msg);
		}
	}

	/**
	 * 有效的消息过滤标识
	 * 
	 * @param msgTypes
	 *            有效的消息类型
	 */
	@Override
	public void iCareWhatType(String... msgTypes) {
		for (String tag : msgTypes)
		{
			controller.willFilter(tag);
		}
	}

	@Override
	public void clearCaredType() {
		controller.clearFilter();
	}

	@Override
	public void reset() {
		controller.reset(0);
	}

	/**
	 * await的同时要进行结果检查
	 * @return
	 */
	@Override
	public boolean await() {
		return await(10);
	}

	@Override
	public boolean await(long timeout) {
		return controller.await(timeout);
	}

	/**
	 * await通过后再等1s，继续收可能出现的预期之外的消息
	 * @return
	 */
	@Override
	public boolean awaitAndSleep() {
		return awaitAndSleep(10, 1);
	}

	/**
	 * 待被测系统发给TMQ的消息通过匹配器的检查，若通过检查则再睡眠指定时长
	 */
	@Override
	public boolean awaitAndSleep(long timeout, long delay) {
		return controller.awaitAndSleep(timeout, delay);
	}

	@Override
	public void setOutStream(OutputStream os)
	{
		controller.setOutInfo(os);
	}

	@Override
	public void printHistory(String head, String foot) {
		if (null != head) {
			controller.print(head.concat(System.getProperty("line.separator")));
		}
		controller.print();
		if (null != foot) {
			controller.print(foot.concat(System.getProperty("line.separator")));
		}
	}

	@Override
	public void printText(String head, String foot, String text) {
		if (null != head) {
			controller.print(head.concat(System.getProperty("line.separator")));
		}
		if (null != text) {
			controller.print(text.concat(System.getProperty("line.separator")));
		}
		if (null != foot) {
			controller.print(foot.concat(System.getProperty("line.separator")));
		}
	}

	@Override
	public void setCheckListener(CheckListener listener) {
		controller.setCheckListener(listener);
	}

	//=========================================================================
	// 只有一个容量的BlockingQueue
	BlockingQueue<Object> tempMsgQueue = new LinkedBlockingQueue<>(1);

	@Override
	public void offerTempMsg(Object o) {
		tempMsgQueue.offer(o);
	}

	@Override
	public Object pollTempMsg(int timeout) {
		try {
			return tempMsgQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void clearTempMsg() {
		tempMsgQueue.clear();
	}
}
