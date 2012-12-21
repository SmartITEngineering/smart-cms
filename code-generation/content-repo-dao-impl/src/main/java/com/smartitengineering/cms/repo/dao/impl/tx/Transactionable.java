package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate dao implementations that should be wrapped with transaction daos. This is a binding annotation.
 * @author imyousuf
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindingAnnotation
public @interface Transactionable {
}
