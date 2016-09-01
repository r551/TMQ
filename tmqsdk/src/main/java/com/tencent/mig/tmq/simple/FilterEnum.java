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
