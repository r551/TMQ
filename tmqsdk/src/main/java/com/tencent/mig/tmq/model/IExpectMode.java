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

import com.tencent.mig.tmq.simple.SimpleTmqMsg;

import java.util.List;

/**
 * 匹配模式
 * @author yoyoqin, kaihuancao
 *
 * @param <M> 匹配的消息类型
 */
public interface IExpectMode<T, M> {
	/**
	 * 预设关注的消息，实际的关注消息记录会随着具体实现在match过程中变化
	 * @param msg
	 */
	void willCare(M msg);

	/**
	 * 清理预期模式中记录的当次预期记录
	 */
	void clear();
	
	/**
	 * 对输入的消息进行匹配
	 * @param msg 要匹配的消息
	 * @return 匹配 true; 不匹配 false
	 */
	boolean match(M msg);

	/**
	 * 判断输入的消息是否是关键消息。
	 * 比如严格校验下所有消息都是关键消息，灵活校验下只有第一次匹配的消息才是关键消息
	 * @param msg 要匹配的消息
	 * @return 匹配 true; 不匹配 false
	 */
	boolean keyMatched(M msg);

	/**
	 * 获取当完成接收最后1个关键信息时的通过过滤的所有消息列表
	 * @return
     */
	int getCountWhenCompleteKeyMatch();
	
	/**
	 * 检查记录中的消息，是否符合本预期模式的预期
	 * @return 返回成功 0, 其他 各种失败情况
	 */
	IRetCode check(ILogger<T, M> logger);

	/**
	 * 在完成匹配消息收录后，检查传入的消息是否匹配过
	 * @param msg 送来检查的消息
	 * @return true 匹配 false 未匹配过
     */
	boolean check(M msg);

	/**
	 * 获取当前尚未命中的预期消息列表
	 */
	List<M> getUnHitMsgList();
}
