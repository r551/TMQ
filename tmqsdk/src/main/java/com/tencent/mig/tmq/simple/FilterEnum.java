package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.IFilter;
import com.tencent.mig.tmq.model.IFilters;

public enum FilterEnum implements IFilters<String, SimpleTmqMsg> {
	TYPE(new SimpleTypeFilter()),
	;

	private IFilter<String, SimpleTmqMsg> filter;

	private FilterEnum(IFilter<String, SimpleTmqMsg> filter) {
		this.filter = filter;
	}

	@Override
	public IFilter<String, SimpleTmqMsg> getFilter() {
		return filter;
	}

}
