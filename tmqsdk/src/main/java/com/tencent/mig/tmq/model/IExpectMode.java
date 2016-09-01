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

/**
 * 匹配模式
 * @author yoyoqin, kaihuancao
 *
 * @param <M> 匹配的消息类型
 */
public interface IExpectMode<T, M> {
	/**
	 * 预设关注的消息
	 * @param msg
	 */
	void willCare(M msg);
	
	/**
	 * 对输入的消息进行匹配
	 * @param msg 要匹配的消息
	 * @return 匹配 true; 不匹配 false
	 */
	boolean match(M msg);
	
	/**
	 * 检查记录中的消息，是否符合本预期模式的预期
	 * @return 返回成功 0, 其他 各种失败情况
	 */
	IRetCode check(ILogger<T, M> logger);
}
