package wse.utils;

import java.net.URISyntaxException;
import java.util.Map;

public class ServiceMethod<IU extends ComplexType, T extends WrappedOperation<?, IU, ?, ?>> {
	private final Class<IU> requestTypeClass;
	private final Class<T> serviceClass;

	public ServiceMethod(Class<IU> requestTypeClass, Class<T> serviceClass) {
		this.requestTypeClass = requestTypeClass;
		this.serviceClass = serviceClass;
	}

	public Map<String, Object> call(Map<String, Object> target, Map<String, Object> options,
			Map<String, Object> request) throws InstantiationException, IllegalAccessException, URISyntaxException {
		T service = serviceClass.newInstance();
		IU input = requestTypeClass.newInstance();

		if (target != null) {
			String protocol = (String) target.get("protocol");
			String host = (String) target.get("host");
			Integer port = (Integer) target.get("port");
			String path = (String) target.get("path");

			if (protocol != null)
				service.setTargetScheme(protocol);
			if (host != null)
				service.setTargetHost(host);
			if (port != null)
				service.setTargetPort(port);
			if (path != null)
				service.setTargetPath(path);
		}

		input.fromMap(request);
		ComplexType response = service.call(input);

		return response.toMap();
	}

	public static <IU extends ComplexType, T extends WrappedOperation<?, IU, ?, ?>> void bind(
			Map<String, ServiceMethod<?, ?>> map, Class<IU> requestTypeClass, Class<T> serviceClass) {
		map.put(serviceClass.getSimpleName(), new ServiceMethod<>(requestTypeClass, serviceClass));
	}

}
