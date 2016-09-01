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

import java.util.HashSet;
import java.util.Set;

/**
 * 默认的消息过滤器实现
 * @author yoyoqin, kaihuancao
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public abstract class DefaultFilter<T, M> implements IFilter<T, M> {
	// 可以预先设置消息按类型过滤
	protected Set<T> filterSet = new HashSet<>();

	@Override
	public void willFilter(T caredType) {
		if (caredType == null) {
			return;
		}

		filterSet.add(caredType);
	}

	@Override
	public void clear() {
		filterSet.clear();
	}
}
