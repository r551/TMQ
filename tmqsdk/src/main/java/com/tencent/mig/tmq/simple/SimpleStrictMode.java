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

import com.tencent.mig.tmq.model.DefaultStrictMode;
import com.tencent.mig.tmq.model.IExclusiveFlag;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

public class SimpleStrictMode<T, M> extends DefaultStrictMode<T, M> {
	/**
	 * 严格匹配模式最后的check事实上并不需要依据这个方法来判断，如果有预期外的消息早在report的时候就
	 * 会立刻检查不通过的
	 * @param msg 送来检查的消息
	 * @return
     */
	@Override
	public boolean check(M msg) {
		return expectedQueue.contains(msg);
	}

	@Override
	public IRetCode check(ILogger<T, M> logger) {
		if (!(expectedQueue.isEmpty() || expectedQueue.peek() instanceof IExclusiveFlag)) {
			return RetCode.NOT_RECEIVED_ALL_EXPECTED_QUEUE_MESSAGE;
		}
		if (logger.getAfterFilterQueue().size() != logger.getCheckedQueue().size()) {
			return RetCode.SIZE_OF_MSG_QUEUE_AND_EXPECTED_QUEUE_NOT_EQUALS;
		}
		for (int i = 0; i < logger.getCheckedQueue().size(); i++) {
			if (!logger.getAfterFilterQueue().get(i).equals(logger.getCheckedQueue().get(i))) {
				return RetCode.MSG_QUEUE_NOT_MATCHED_EXPECT_EDQUEUE;
			}
		}

		return RetCode.SUCCESS;
	}
}
