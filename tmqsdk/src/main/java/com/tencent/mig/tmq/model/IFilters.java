package com.tencent.mig.tmq.model;

public interface IFilters<T, M> {
	IFilter<T, M> getFilter();
}
