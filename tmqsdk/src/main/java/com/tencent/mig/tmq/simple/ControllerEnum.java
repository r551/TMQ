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

import com.tencent.mig.tmq.model.IExecuteController;
import com.tencent.mig.tmq.model.IExecuteControllers;

public enum ControllerEnum implements IExecuteControllers<String, SimpleTmqMsg> {
	SIMPLE(new SimpleController()),
	;

	private IExecuteController<String, SimpleTmqMsg> controller;
	
	private ControllerEnum(IExecuteController<String, SimpleTmqMsg> controller)
	{
		this.controller = controller;
	}
	
	@Override
	public IExecuteController<String, SimpleTmqMsg> getController() {
		return controller;
	}

}
