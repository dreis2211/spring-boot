/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.context;

import org.springframework.test.context.BootstrapContext;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * {@link SpringBootTestContextBootstrapperCustomizer} for Reactive related tests.
 *
 * @see SpringBootTestContextBootstrapper
 * @since 2.1.0
 * @author Christoph Dreis
 */
public class ReactiveWebSpringBootTestContextBootstrapperCustomizer
		extends DefaultSpringBootTestContextBootstrapperCustomizer {

	@Override
	public MergedContextConfiguration customizeMergedConfiguration(
			BootstrapContext bootstrapContext, MergedContextConfiguration mergedConfig) {
		return new ReactiveWebMergedContextConfiguration(
				super.customizeMergedConfiguration(bootstrapContext, mergedConfig));
	}

}
