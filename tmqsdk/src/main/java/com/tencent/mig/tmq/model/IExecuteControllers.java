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
 * 切换匹配模式时，可用的匹配模式作为枚举类型实现此接口，通过此方式使tmq框架代码符合开闭原则
 * 注：这种枚举接口的方式在简单系统上做开闭原则这是第一次尝试，应该比工厂模式方案实现的开闭原则要简单
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface IExecuteControllers<T, M> {
	IExecuteController<T, M> getController();
}
