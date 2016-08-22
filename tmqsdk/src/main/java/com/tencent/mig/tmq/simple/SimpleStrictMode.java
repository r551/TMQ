package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.DefaultStrictMode;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

public class SimpleStrictMode<T, M> extends DefaultStrictMode<T, M> {
	@Override
	public IRetCode check(ILogger<T, M> logger) {
		if (!expectedQueue.isEmpty()) {
			return RetCode.NOT_RECEIVED_ALL_EXPECTED_QUEUE_MESSAGE;
		}
		if (logger.getAfterFilterQueue().size() != logger.getCheckedQueue().size()) {
			return RetCode.SIZE_OF_MSG_QUEUE_AND_EXPECTED_QUEUE_NOT_EQUALS;
		}
		for (int i = 0; i < logger.getCheckedQueue().size(); i++) {
			if (!logger.getAfterFilterQueue().get(i).equals(logger.getCheckedQueue().poll())) {
				return RetCode.MSG_QUEUE_NOT_MATCHED_EXPECT_EDQUEUE;
			}
		}

		return RetCode.SUCCESS;
	}
}
