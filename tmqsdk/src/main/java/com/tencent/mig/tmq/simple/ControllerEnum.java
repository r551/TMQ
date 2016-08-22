package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.IExecuteController;
import com.tencent.mig.tmq.model.IExecuteControllers;

public enum ControllerEnum implements IExecuteControllers<String, SimpleTmqMsg> {
	SIMPLE(new SimpleController()),
	;

	private IExecuteController<String, SimpleTmqMsg> controller;
	
	private ControllerEnum(IExecuteController<String, SimpleTmqMsg> controller)
	{
		this.controller = controller;
	}
	
	@Override
	public IExecuteController<String, SimpleTmqMsg> getController() {
		return controller;
	}

}
