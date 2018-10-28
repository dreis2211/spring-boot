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

import org.springframework.boot.WebApplicationType;
import org.springframework.util.ClassUtils;

/**
 * Utility class that can be used to deduce the {@link WebApplicationType} from the
 * classes on the classpath.
 *
 * @author Christoph Dreis
 * @since 2.1.0
 */
final class WebApplicationTypeDeducer {

	private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet",
			"org.springframework.web.context.ConfigurableWebApplicationContext" };

	private static final boolean IS_REACTIVE_WEB_ENVIRONMENT_CLASS_PRESENT = ClassUtils
			.isPresent("org.springframework.web.reactive.DispatcherHandler", null);

	private static final boolean IS_MVC_WEB_ENVIRONMENT_CLASS_PRESENT = ClassUtils
			.isPresent("org.springframework.web.servlet.DispatcherServlet", null);

	private static final boolean IS_JERSEY_WEB_ENVIRONMENT_CLASS_PRESENT = ClassUtils
			.isPresent("org.glassfish.jersey.server.ResourceConfig", null);

	private WebApplicationTypeDeducer() {
	}

	/**
	 * Deduce the {@link WebApplicationType} from the classpath.
	 * @return the deduced {@link WebApplicationType}
	 */
	static WebApplicationType deduceWebApplicationType() {
		if (IS_REACTIVE_WEB_ENVIRONMENT_CLASS_PRESENT
				&& !IS_MVC_WEB_ENVIRONMENT_CLASS_PRESENT
				&& !IS_JERSEY_WEB_ENVIRONMENT_CLASS_PRESENT) {
			return WebApplicationType.REACTIVE;
		}
		for (String className : WEB_ENVIRONMENT_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) {
				return WebApplicationType.NONE;
			}
		}
		return WebApplicationType.SERVLET;
	}

	/**
	 * Check if deduced {@link WebApplicationType} is of the given type.
	 * @param webApplicationType the {@link WebApplicationType}
	 * @return the deduced {@link WebApplicationType}
	 * @see WebApplicationTypeDeducer#deduceWebApplicationType()
	 */
	static boolean isWebApplicationType(WebApplicationType webApplicationType) {
		return deduceWebApplicationType() == webApplicationType;
	}

}
