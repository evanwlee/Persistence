package com.evanwlee.data.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //on class level
public @interface UsesDataAnnotations {

		public enum Type {
		   COMPOSITE,
		   REQUIRED
		}

		Type type() default Type.COMPOSITE;
		
		String[] tags() default "";
		

	}
