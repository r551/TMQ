package com.tencent.mig.tmq.model;

public interface IExecuteControllers<T, M> {
	IExecuteController<T, M> getController();
}
