package wse.server.listener;

import java.io.IOException;
import java.lang.reflect.Method;

import wse.server.CallTreatment;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.ComplexType;

public abstract class ServiceListener<T extends ComplexType, L extends ComplexType> implements CallTreatment {
	
	@Override
	public void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
	}
	
	public abstract void treatServiceASync(T request, L response);

	public static final Method treatServiceMethod;
	static {
		treatServiceMethod = ServiceListener.class.getDeclaredMethods()[0];
	}
}
