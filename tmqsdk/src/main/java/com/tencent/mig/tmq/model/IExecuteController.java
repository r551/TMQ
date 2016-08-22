package com.tencent.mig.tmq.model;

import java.io.OutputStream;

public interface IExecuteController<T, M> {
	boolean report(T tag, Object msg);
	boolean await(long timeout);
	boolean check();
	void clear();
	void reset(int countInit);
	void willCare(M msg);
	void willFilter(T tag);
	void clearFilter();
	void switchExpectMode(IExpectModes<T, M> mode);
	void switchFilter(IFilters<T, M> filter);
	void print();
	void print(String text);
	void setOutInfo(OutputStream os);
	void setOutFlag(boolean flag);
}
