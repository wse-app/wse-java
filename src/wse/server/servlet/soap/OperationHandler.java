package wse.server.servlet.soap;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
/**
 * Value of OperationHandler represents the SOAPAction of which this Operation
 * is bound to
 */
public @interface OperationHandler {
	/**
	 * Value of OperationHandler represents the SOAPAction of which this Operation
	 * is bound to
	 */
	String value();
}
