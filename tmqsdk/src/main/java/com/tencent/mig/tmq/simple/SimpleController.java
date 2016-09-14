/*
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.mig.tmq.simple;

import com.tencent.mig.tmq.model.DefaultLog;
import com.tencent.mig.tmq.model.IExecuteController;
import com.tencent.mig.tmq.model.IExpectMode;
import com.tencent.mig.tmq.model.IExpectModes;
import com.tencent.mig.tmq.model.IFilter;
import com.tencent.mig.tmq.model.IFilters;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleController implements IExecuteController<String, SimpleTmqMsg> {
	private ILogger<String, SimpleTmqMsg> logger;
	private IExpectMode<String, SimpleTmqMsg> mode;
	private IFilter<String, SimpleTmqMsg> filter;
	private CountDownLatch countDownLatch = null;

	private OutputStream os;
	private boolean outFlag;

	public SimpleController()
	{
		this.logger = new DefaultLog<>();
	}

	@Override
	public boolean report(String tag, Object msg) {
		synchronized (this) {
			if (countDownLatch == null || countDownLatch.getCount() == 0) {
				return true;
			}

			SimpleTmqMsg tmqMsg = msg instanceof SimpleTmqMsg ? (SimpleTmqMsg) msg : new SimpleTmqMsg(tag, msg.toString());

			if (logger.append(tmqMsg) && mode.match(tmqMsg)) {
				logger.appendCheckedMsg(tmqMsg);
				countDownLatch.countDown();
				return true;
			}
			return true;
		}
	}

	@Override
	public boolean await(long timeout) {
		if (countDownLatch == null) {
			return false;
		}

		try {
			countDownLatch.await(timeout, TimeUnit.SECONDS);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean check() {
		IRetCode result = mode.check(logger);
		if (outFlag) {
			print();
			print(result.getDesc() + System.getProperty("line.separator"));
		} else if (result != RetCode.SUCCESS) {
			print(result.getDesc() + System.getProperty("line.separator"));
		}

		return result == RetCode.SUCCESS;
	}

	@Override
	public void switchFilter(IFilters filter) {
		this.filter = filter.getFilter();
		this.logger.setFilter(filter.getFilter());
	}

	@Override
	public void switchExpectMode(IExpectModes mode) {
		 this.mode = mode.getMode();
	}

	@Override
	public void print() {
		if (null == os) {
			return;
		}
		String[] logs = logger.getHistory();
		try {
			// 注意换行用\r\n在PrintStream中是无效的
			os.write("====pre Filter MsgQueue:====".getBytes());
			os.write(logs[0].getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.write("====after Filter MsgQueue:====".getBytes());
			os.write(logs[1].getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.write("====checked MsgQueue:====".getBytes());
			os.write(logs[2].getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void print(String text) {
		if (os == null) return;
		try {
			os.write(text.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clear() {
		logger.clear();
		filter.clear();
	}

	@Override
	public void setOutInfo(OutputStream os) {
		this.os = os;
	}

	@Override
	public void setOutFlag(boolean flag) {
		this.outFlag = flag;
	}

	@Override
	public void willCare(SimpleTmqMsg msg) {
		mode.willCare(msg);
	}

	@Override
	public void willFilter(String tag) {
		filter.willFilter(tag);
	}

	@Override
	public void clearFilter() {
		filter.clear();
	}

	@Override
	public void reset(int countInit) {
		clear();
		countDownLatch = new CountDownLatch(countInit);
	}
}
