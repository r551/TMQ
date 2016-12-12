package com.tencent.mig.tmq.model;

import java.util.Collection;

/**
 * Created by yoyoqin on 2016/12/9.
 */
public interface CheckListener {
     public void onCheck(IRetCode retCode,
                         Collection msgPreFilter, Collection msgAfterFilter, Collection msgChecked,
                         String[] msgGroupArray);
}
