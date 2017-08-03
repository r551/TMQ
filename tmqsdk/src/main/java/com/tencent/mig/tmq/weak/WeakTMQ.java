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
package com.tencent.mig.tmq.weak;

import com.tencent.mig.tmq.abs.AbstractTMQ;
import com.tencent.mig.tmq.model.IExclusiveFlag;
import com.tencent.mig.tmq.simple.ControllerEnum;
import com.tencent.mig.tmq.simple.FilterEnum;
import com.tencent.mig.tmq.simple.ModeEnum;
import com.tencent.mig.tmq.simple.SimpleTmqMsg;

public class WeakTMQ extends AbstractTMQ<Object> {

	public WeakTMQ()
	{
		super(ControllerEnum.WEAK, FilterEnum.TYPE, ModeEnum.STRICT);
	}

	/**
	 * 有效的消息序列初始化
	 * 
	 * @param tmqMsgList
	 *            有效的消息序列
	 */
	@Override
	public void iCareWhatMsg(Object... tmqMsgList) {
		/*
		 * FIXME weak消息中继续借用SimpleTmqMsg.NULL做排他逻辑
		 * 增加设置null值为预期不应该收到任何消息的逻辑
		 */
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
			Object msg = tmqMsgList[indexBeforeExclusiveFlag];
			if (msg == null || msg instanceof IExclusiveFlag)
			{
				hasExclusiveMsg = true;
				break;
			}
		}

		controller.reset(indexBeforeExclusiveFlag);
		for (Object msg : tmqMsgList) // 这个地方加倒是可以都加上
		{
			// 单条msg有可能是null，此时语义代表为在null后不应该收到其他通过过滤器的任何消息
			controller.willCare(msg);
		}
	}
}
