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
package com.tencent.mig.tmq.abs;

import com.tencent.mig.tmq.model.CheckListener;
import com.tencent.mig.tmq.model.DefaultLog;
import com.tencent.mig.tmq.model.IExecuteController;
import com.tencent.mig.tmq.model.IExpectMode;
import com.tencent.mig.tmq.model.IExpectModes;
import com.tencent.mig.tmq.model.IFilter;
import com.tencent.mig.tmq.model.IFilters;
import com.tencent.mig.tmq.model.ILogger;
import com.tencent.mig.tmq.model.IRetCode;
import com.tencent.mig.tmq.simple.RetCode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

abstract public class AbstractController<M> implements IExecuteController<String, M> {
	protected ILogger<String, M> logger;
	protected IExpectMode<String, M> mode;
	protected IFilter<String, M> filter;
	protected CheckListener<M> checkListener;
	protected CountDownLatch countDownLatch = null;

	/*
	 * 在reset后，是否已通过一次willCare方法触发启动。在reset方法中应将其置为false
	 * TODO 后续若Controller的状态变复杂，应按状态模式重构
	 */
	protected boolean state;

	protected OutputStream os;
	protected boolean outFlag;

	public AbstractController()
	{
		this.logger = new DefaultLog<>();
	}

	@Override
	public boolean await(long timeout) {
		if (countDownLatch == null) {
			return false;
		}

		try {
			return countDownLatch.await(timeout, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean awaitAndSleep(long timeout, long delay) {
		if (countDownLatch == null) {
			return false;
		}

		try {
			boolean ret = countDownLatch.await(timeout, TimeUnit.SECONDS);
			if (ret)
			{
				TimeUnit.SECONDS.sleep(delay);
			}
			return ret;
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

		if (null != checkListener)
		{
			// 为了方便消息多时的问题定位，将未命中的消息也加入回调
			String[] logs = logger.getHistory();
			String[] strongLogs = new String[4];
			strongLogs[0] = logs[0];
			strongLogs[1] = logs[1];
			strongLogs[2] = logs[2];
			strongLogs[3] = "";
			List<M> uncheckedMsgList = mode.getUnHitMsgList();
			if (null != uncheckedMsgList && !uncheckedMsgList.isEmpty())
			{
				StringBuilder uncheckedMsg = new StringBuilder();
				for (M sm : uncheckedMsgList)
				{
					uncheckedMsg.append(sm);
					uncheckedMsg.append(System.getProperty("line.separator"));
				}
				strongLogs[3] = uncheckedMsg.toString();
			}

			checkListener.onCheck(result,
					logger.getPreFilterQueue(), logger.getAfterFilterQueue(), logger.getCheckedQueue(),
					strongLogs);
		}

		return result == RetCode.SUCCESS;
	}

	@Override
	public void switchFilter(IFilters filter) {
		// 老的filter要先清理
		if (this.filter != null)
		{
			this.filter.clear();
		}
		this.filter = filter.getFilter();
		this.logger.setFilter(filter.getFilter());
	}

	@Override
	public void switchExpectMode(IExpectModes mode) {
		// 老的mode要先清理
		if (this.mode != null)
		{
			this.mode.clear();
		}
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
			os.write("====pre Filter MsgQueue====".getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.write(logs[0].getBytes());

			os.write("====after Filter MsgQueue====".getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.write(logs[1].getBytes());

			os.write("====checked MsgQueue====".getBytes());
			os.write(System.getProperty("line.separator").getBytes());
			os.write(logs[2].getBytes());

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
		mode.clear();
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
	public void willFilter(String tag) {
		filter.willFilter(tag);
	}

	@Override
	public void clearFilter() {
		filter.clear();
	}

	@Override
	public void reset(int countInit) {
		state = false;
		clear();
		countDownLatch = new CountDownLatch(countInit);
	}

	@Override
	public void setCheckListener(CheckListener listener) {
		this.checkListener = listener;
	}
}
