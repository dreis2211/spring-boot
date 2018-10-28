package org.springframework.boot.test.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.MergedContextConfiguration;

/**
 * Annotation that can be specified in combination with
 * {@link org.springframework.test.context.BootstrapWith @BootstrapWith} in order to
 * customize parts of the context bootstrapping process.
 *
 * @author Christoph Dreis
 * @since 2.1.0
 * @see SpringBootTestContextBootstrapper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CustomizeBootstrapWith {

	/**
	 * The {@link SpringBootTestContextBootstrapperCustomizer} to be used during context
	 * bootstrapping.
	 * @see SpringBootTestContextBootstrapper#processMergedContextConfiguration(MergedContextConfiguration)
	 * @return the class of {@link SpringBootTestContextBootstrapperCustomizer} to be used
	 */
	Class<? extends SpringBootTestContextBootstrapperCustomizer> value() default DefaultSpringBootTestContextBootstrapperCustomizer.class;

}
