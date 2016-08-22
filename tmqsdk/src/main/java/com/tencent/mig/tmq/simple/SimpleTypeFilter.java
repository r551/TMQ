package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.DefaultFilter;

/**
 * Created by yoyoqin on 2016/8/21.
 */
public class SimpleTypeFilter<T> extends DefaultFilter<T, SimpleTmqMsg> {
    @Override
    public boolean validate(SimpleTmqMsg msg) {
        if (msg == null || msg.tag == null)
            return false;

        // filterSet为空默认所有类型消息都有效
        if (filterSet.isEmpty()) {
            return true;
        }

        return filterSet.contains(msg.tag) ? true : false;
    }
}
