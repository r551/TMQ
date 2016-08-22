package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.IExpectMode;
import com.tencent.mig.tmq.model.IExpectModes;

public enum ModeEnum implements IExpectModes<String, SimpleTmqMsg> {
	STRICT(new SimpleStrictMode<String, SimpleTmqMsg>()),
	;

	private IExpectMode<String, SimpleTmqMsg> mode;
	
	private ModeEnum(IExpectMode<String, SimpleTmqMsg> mode)
	{
		this.mode = mode;
	}
	
	@Override
	public IExpectMode<String, SimpleTmqMsg> getMode() {
		return this.mode;
	}
}
