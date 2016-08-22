package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.IRetCode;

public enum RetCode implements IRetCode {
	SUCCESS("success"),
	NOT_RECEIVED_ALL_EXPECTED_QUEUE_MESSAGE("not received all expectedQueue message"),
	SIZE_OF_MSG_QUEUE_AND_EXPECTED_QUEUE_NOT_EQUALS("size of msgQueue and expectedQueue not equals"),
	MSG_QUEUE_NOT_MATCHED_EXPECT_EDQUEUE("message sequence in msgQueue not matched message sequence in expectedQueue");

	private String desc;
	
	private RetCode(String desc)
	{
		this.desc = desc;
	}
	
	public String getDesc()
	{
		return this.desc;
	}
}
