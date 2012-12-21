package com.smartitengineering.cms.repo.dao.tx;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for notifying that a method should start a transaction if not already started. Transaction Propagation
 * @author imyousuf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@BindingAnnotation
public @interface Transactional {

  /**
   * Configures this transaction to be an isolated from rest of the instance or whether it should not care of it.
   * @return By default its true; i.e. isolated from other parallel transactions
   */
  boolean isolated() default true;

  /**
   * Configures whether the transaction requires a new transaction or can use the existing one
   * @return By default its true; i.e. transaction will be propagated
   */
  boolean propagationRequired() default true;
}
