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
 * Interface that can be used to provide custom functionality for
 * {@link SpringBootTestContextBootstrapper}.
 *
 * @author Christoph Dreis
 * @since 2.1.0
 * @see SpringBootTestContextBootstrapper
 */
interface SpringBootTestContextBootstrapperCustomizer {

	/**
	 * Customize the supplied {@link MergedContextConfiguration} instance.
	 * <p>
	 * The returned {@link MergedContextConfiguration} instance may be a wrapper around or
	 * a replacement of the original instance.
	 * <p>
	 * Concrete subclasses may choose to return a specialized subclass of
	 * {@link MergedContextConfiguration} based on properties in the supplied instance.
	 * @param bootstrapContext the {@code BootstrapContext}
	 * @param mergedConfig the {@code MergedContextConfiguration} to process; never
	 * {@code null}
	 * @return a fully initialized {@code MergedContextConfiguration}; never {@code null}
	 */
	MergedContextConfiguration customizeMergedConfiguration(
			BootstrapContext bootstrapContext, MergedContextConfiguration mergedConfig);

}
