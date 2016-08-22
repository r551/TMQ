package com.tencent.mig.tmq.model;

import java.io.OutputStream;

/**
 * Created by yoyoqin on 2016/8/20.
 */
public interface ITmq<T, M> {
    ITmq setExecuteController(IExecuteControllers<T, M> controller);
    ITmq switchFilter(IFilters<T, M> filter);
    ITmq switchExpectMode(IExpectModes<T, M> mode);
    boolean check();
    boolean report(T tag, Object msg);
    void iCareWhatMsg(M... tmqMsgList);
    void iCareWhatType(T... msgTypes);
    void clearCaredType();
    void reset();
    boolean await();
    boolean await(long timeout);
    void setOutStream(OutputStream os);
    void printHistory(String head, String foot);
    void printText(String head, String foot, String text);
}
