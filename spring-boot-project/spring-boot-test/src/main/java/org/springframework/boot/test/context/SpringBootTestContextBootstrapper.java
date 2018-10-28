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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link TestContextBootstrapper} for Spring Boot. Provides support for
 * {@link SpringBootTest @SpringBootTest} and may also be used directly or subclassed.
 * Provides the following features over and above {@link DefaultTestContextBootstrapper}:
 * <ul>
 * <li>Uses {@link SpringBootContextLoader} as the
 * {@link #getDefaultContextLoaderClass(Class) default context loader}.</li>
 * <li>Automatically searches for a
 * {@link SpringBootConfiguration @SpringBootConfiguration} when required.</li>
 * <li>Allows custom {@link Environment} {@link #getProperties(Class)} to be defined.</li>
 * <li>Provides support for different {@link WebEnvironment webEnvironment} modes.</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @author Madhura Bhave
 * @since 1.4.0
 * @see SpringBootTest
 * @see TestConfiguration
 */
public class SpringBootTestContextBootstrapper extends DefaultTestContextBootstrapper {

	private static final String ACTIVATE_SERVLET_LISTENER = "org.springframework.test."
			+ "context.web.ServletTestExecutionListener.activateListener";

	@Override
	public TestContext buildTestContext() {
		TestContext context = super.buildTestContext();
		verifyConfiguration(context.getTestClass());
		WebEnvironment webEnvironment = getWebEnvironment(context.getTestClass());
		if (webEnvironment == WebEnvironment.MOCK && WebApplicationTypeDeducer
				.isWebApplicationType(WebApplicationType.SERVLET)) {
			context.setAttribute(ACTIVATE_SERVLET_LISTENER, true);
		}
		else if (webEnvironment != null && webEnvironment.isEmbedded()) {
			context.setAttribute(ACTIVATE_SERVLET_LISTENER, false);
		}
		return context;
	}

	@Override
	protected Set<Class<? extends TestExecutionListener>> getDefaultTestExecutionListenerClasses() {
		Set<Class<? extends TestExecutionListener>> listeners = super.getDefaultTestExecutionListenerClasses();
		List<DefaultTestExecutionListenersPostProcessor> postProcessors = SpringFactoriesLoader
				.loadFactories(DefaultTestExecutionListenersPostProcessor.class,
						getClass().getClassLoader());
		for (DefaultTestExecutionListenersPostProcessor postProcessor : postProcessors) {
			listeners = postProcessor.postProcessDefaultTestExecutionListeners(listeners);
		}
		return listeners;
	}

	@Override
	protected ContextLoader resolveContextLoader(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributesList) {
		Class<?>[] classes = getClasses(testClass);
		if (!ObjectUtils.isEmpty(classes)) {
			for (ContextConfigurationAttributes configAttributes : configAttributesList) {
				addConfigAttributesClasses(configAttributes, classes);
			}
		}
		return super.resolveContextLoader(testClass, configAttributesList);
	}

	private void addConfigAttributesClasses(
			ContextConfigurationAttributes configAttributes, Class<?>[] classes) {
		List<Class<?>> combined = new ArrayList<>();
		combined.addAll(Arrays.asList(classes));
		if (configAttributes.getClasses() != null) {
			combined.addAll(Arrays.asList(configAttributes.getClasses()));
		}
		configAttributes.setClasses(ClassUtils.toClassArray(combined));
	}

	@Override
	protected Class<? extends ContextLoader> getDefaultContextLoaderClass(
			Class<?> testClass) {
		return SpringBootContextLoader.class;
	}

	@Override
	protected MergedContextConfiguration processMergedContextConfiguration(
			MergedContextConfiguration mergedConfig) {
		SpringBootTestContextBootstrapperCustomizer contextConfigurer = getBootstrapCustomizer(
				mergedConfig.getTestClass());
		return contextConfigurer.customizeMergedConfiguration(getBootstrapContext(),
				mergedConfig);
	}

	/**
	 * Return the {@link SpringBootTestContextBootstrapperCustomizer} for this test.
	 * @param testClass the source test class
	 * @return the {@link SpringBootTestContextBootstrapperCustomizer}
	 */
	protected SpringBootTestContextBootstrapperCustomizer getBootstrapCustomizer(
			Class<?> testClass) {
		CustomizeBootstrapWith annotation = AnnotatedElementUtils
				.getMergedAnnotation(testClass, CustomizeBootstrapWith.class);

		Class<? extends SpringBootTestContextBootstrapperCustomizer> customizer = (annotation != null)
				? annotation.value() : null;

		if (customizer == ReactiveWebSpringBootTestContextBootstrapperCustomizer.class) {
			return new ReactiveWebSpringBootTestContextBootstrapperCustomizer();
		}
		else if (customizer == WebSpringBootTestContextBootstrapperCustomizer.class) {
			return new WebSpringBootTestContextBootstrapperCustomizer();
		}

		return new DefaultSpringBootTestContextBootstrapperCustomizer();
	}

	/**
	 * Return the {@link WebEnvironment} type for this test or null if undefined.
	 * @param testClass the source test class
	 * @return the {@link WebEnvironment} or {@code null}
	 */
	protected WebEnvironment getWebEnvironment(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.webEnvironment() : null;
	}

	protected Class<?>[] getClasses(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.classes() : null;
	}

	protected String[] getProperties(Class<?> testClass) {
		SpringBootTest annotation = getAnnotation(testClass);
		return (annotation != null) ? annotation.properties() : null;
	}

	protected SpringBootTest getAnnotation(Class<?> testClass) {
		return AnnotatedElementUtils.getMergedAnnotation(testClass, SpringBootTest.class);
	}

	protected void verifyConfiguration(Class<?> testClass) {
		SpringBootTest springBootTest = getAnnotation(testClass);
		if (springBootTest != null
				&& (springBootTest.webEnvironment() == WebEnvironment.DEFINED_PORT
						|| springBootTest.webEnvironment() == WebEnvironment.RANDOM_PORT)
				&& getAnnotation(WebAppConfiguration.class, testClass) != null) {
			throw new IllegalStateException("@WebAppConfiguration should only be used "
					+ "with @SpringBootTest when @SpringBootTest is configured with a "
					+ "mock web environment. Please remove @WebAppConfiguration or "
					+ "reconfigure @SpringBootTest.");
		}
	}

	private <T extends Annotation> T getAnnotation(Class<T> annotationType,
			Class<?> testClass) {
		return AnnotatedElementUtils.getMergedAnnotation(testClass, annotationType);
	}

}
