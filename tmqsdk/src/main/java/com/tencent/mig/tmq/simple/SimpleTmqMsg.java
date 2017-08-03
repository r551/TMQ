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

import com.tencent.mig.tmq.model.IExclusiveFlag;
import com.tencent.mig.tmq.model.IExpectMode;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

import java.util.List;

public class SimpleTmqMsg extends TagTmqMsg {
	String msg;

	public SimpleTmqMsg(String tag, String msg) {
		this.tag = tag;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return tag + "-" + msg;
	}

	@Override
	public boolean equals(Object another) {
		if (null == another)
			return false;
		if (this == another)
			return true;
		if (this.getClass() != another.getClass())
			return false;
		SimpleTmqMsg tmqMsg = (SimpleTmqMsg) another;
		return tag.equals(tmqMsg.tag) && msg.equals(tmqMsg.msg);
	}

	@Override
	public int hashCode() {
		return tag.hashCode() ^ msg.hashCode();
	}

	/**
	 * NULL消息代表预期之外一条消息都不收，收到一条消息即判定不通过
	 */
	public static SimpleTmqMsg NULL = new SimpleExclusiveFlagMsg("**Null","expect no message");

	/**
	 * KEY_MATCHED_NULL消息代表收完关键消息后，预期之外一条消息都不收
	 */
	public static SimpleTmqMsg KEY_MATCHED_NULL = new AfterKeyMatchedExclusiveFlagMsg("**KEY_MATCHED_NULL","expect no message after key matched");

	public static class SimpleExclusiveFlagMsg
			extends SimpleTmqMsg implements IExclusiveFlag<String, SimpleTmqMsg>
	{
		public SimpleExclusiveFlagMsg(String tag, String m) {
			super(tag, m);
		}

		@Override
		public IRetCode exclusiveCheck(IExpectMode<String, SimpleTmqMsg> mode, ILogger<String, SimpleTmqMsg> logger) {
			List<SimpleTmqMsg> afterFilterQueue = logger.getAfterFilterQueue();
			// 如果收到其他消息，即判定为不通过
			for (int i = 0; i < afterFilterQueue.size(); i++)
			{
				SimpleTmqMsg m = afterFilterQueue.get(i);
				if (!mode.check(m))
				{
					return RetCode.RECEIVED_OTHER_MESSAGES_EXCLUSIVE_EXPECT_MESSAGE;
				}
			}
			return RetCode.SUCCESS;
		}
	}

	public static class AfterKeyMatchedExclusiveFlagMsg
			extends SimpleTmqMsg implements IExclusiveFlag<String, SimpleTmqMsg>
	{
		public AfterKeyMatchedExclusiveFlagMsg(String tag, String m) {
			super(tag, m);
		}

		@Override
		public IRetCode exclusiveCheck(
				IExpectMode<String, SimpleTmqMsg> mode, ILogger<String, SimpleTmqMsg> logger) {
			List<SimpleTmqMsg> afterFilterQueue = logger.getAfterFilterQueue();
			// 在关键消息验证完成后，不应该再收到非关键消息类型的其他消息了
			for (int i = mode.getCountWhenCompleteKeyMatch(); i < afterFilterQueue.size(); i++)
			{
				SimpleTmqMsg m = afterFilterQueue.get(i);
				if (!mode.check(m))
				{
					return RetCode.RECEIVED_OTHER_MESSAGES_AFTER_EXCLUSIVE_EXPECT_MESSAGE;
				}
			}
			return RetCode.SUCCESS;
		}
	}
}
