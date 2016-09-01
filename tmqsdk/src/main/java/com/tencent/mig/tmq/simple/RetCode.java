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
