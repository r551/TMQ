package com.tencent.mig.tmq.weak;

import com.tencent.mig.tmq.simple.TagTmqMsg;

/**
 * Created by yoyoqin on 2017/8/2.
 */

public class WeakTmqMsg extends TagTmqMsg {
    Object msg; // 消息体

    public WeakTmqMsg(String tag, Object msg)
    {
        // 弱校验消息，只能以tag作为消息的唯一标识
        this.tag = tag;
        this.msg = msg;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return tag + "-" + msg.toString();
    }

    @Override
    public boolean equals(Object another) {
        if (null == another)
            return false;
        if (this == another)
            return true;
        if (this.getClass() != another.getClass())
            return false;
        WeakTmqMsg tmqMsg = (WeakTmqMsg) another;
        return tag.equals(tmqMsg.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
