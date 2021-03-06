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

import com.tencent.mig.tmq.model.IExpectMode;
import com.tencent.mig.tmq.model.IExpectModes;

public enum ModeEnum implements IExpectModes {
	STRICT(new SimpleStrictMode()),
	FLEXIBLE(new SimpleFlexibleMode()),
	;

	private IExpectMode mode;
	
	private ModeEnum(IExpectMode mode)
	{
		this.mode = mode;
	}
	
	@Override
	public IExpectMode getMode() {
		return this.mode;
	}
}
