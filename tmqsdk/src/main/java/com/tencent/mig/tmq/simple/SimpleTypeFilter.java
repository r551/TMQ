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

import com.tencent.mig.tmq.model.DefaultFilter;

/**
 * Created by yoyoqin on 2016/8/21.
 */
public class SimpleTypeFilter<T> extends DefaultFilter<T, TagTmqMsg> {
    @Override
    public boolean validate(TagTmqMsg msg) {
        if (msg == null || msg.tag == null)
            return false;

        // filterSet为空默认所有类型消息都有效
        if (filterSet.isEmpty()) {
            return true;
        }

        return filterSet.contains(msg.tag) ? true : false;
    }
}
