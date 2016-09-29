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

public class SimpleTmqMsg {
	String tag;
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
	 * NULL消息代表预期一条消息都不收，收到一条消息即判定不通过
	 */
	public static SimpleTmqMsg NULL = new SimpleTmqMsg("**Null","expect no message");
}
