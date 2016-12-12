package com.tencent.mig.tmq.model;

import java.util.Collection;
import java.util.List;

/**
 * Created by yoyoqin on 2016/12/9.
 */
public interface CheckListener<M> {
     public void onCheck(IRetCode retCode,
                         List<M> msgPreFilter, List<M> msgAfterFilter, List<M> msgChecked,
                         String[] msgGroupArray);
}
