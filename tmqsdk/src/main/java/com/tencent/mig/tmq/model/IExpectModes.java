package com.tencent.mig.tmq.model;

public interface IExpectModes<T, M> {
	IExpectMode<T, M> getMode();
}
