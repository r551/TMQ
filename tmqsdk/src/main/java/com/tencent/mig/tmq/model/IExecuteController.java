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

import java.io.OutputStream;

/**
 * TODO 应该实现一个默认实现DefaultController放在model中，避免model层的接口之间没有实际的关联
 * 测试执行控制器接口
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface IExecuteController<T, M> {
	/**
	 * 被测代码中调用report方法，将消息发给TMQ测试框架
	 * @param tag 消息标签
	 * @param msg 消息内容，TMQ框架不持有msg对象，不会造成内存泄露
	 * @return 消息上报成功，目前全部返回true
	 */
	boolean report(T tag, Object msg);

	/**
	 * 等待被测系统发给TMQ的消息通过匹配器的检查
	 * @param timeout 超时时间
	 * @return 成功通过匹配器检查 true，到超时时间还没通过 false
	 */
	boolean await(long timeout);

	/**
	 * 在完成等待接收消息流程后，检查收到的消息是否与预期匹配
	 * @return 匹配 true, 不匹配 false
	 */
	boolean check();

	/**
	 * 清空预设的关注消息序列、预设的有效消息标签和当前用例已记录的消息列表
	 */
	void clear();

	/**
	 * 重置TMQ框架状态，
	 * 包括：
	 * 清空预设的关注消息序列、预设的有效消息标签和当前用例已记录的消息列表，清理流程控制状态标志。
	 * 但不包括：设置过滤器、设置检查模式
	 */
	void reset(int countInit);

	/**
	 * 预设有效的消息，与匹配模式配合进行消息的校验
	 * @param msg 有效的消息序列
	 */
	void willCare(M msg);

	/**
	 * 预设有效的消息标签，与过滤器配合进行初步的消息过滤
	 * @param tag 有效的消息标签序列
	 */
	void willFilter(T tag);

	/**
	 * 清理已在过滤器中设置消息标签
	 */
	void clearFilter();

	/**
	 * 切换匹配模式
	 * @param mode 匹配模式
	 */
	void switchExpectMode(IExpectModes<T, M> mode);

	/**
	 * 切换过滤器
	 * @param filter 过滤器
	 */
	void switchFilter(IFilters<T, M> filter);

	/**
	 * 设置已记录消息的输出流
	 * @param os 输出流
	 */
	void setOutInfo(OutputStream os);

	/**
	 * 设置是否向已设置的输出流输出
	 * @param flag 输出控制开关，输出 true，不输出 false
	 */
	void setOutFlag(boolean flag);
	/**
	 * 向已设置的输出流打印用例执行过程中记录的消息情况
	 */
	void print();

	/**
	 * 向已设置的输出流打印一行文本
	 * @param text 打印的文本行
	 */
	void print(String text);

}
