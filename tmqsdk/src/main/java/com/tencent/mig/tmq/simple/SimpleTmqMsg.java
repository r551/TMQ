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
}
