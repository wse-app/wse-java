package wse.utils;

import java.net.URISyntaxException;

import wse.utils.exception.WseException;

public abstract class WrappedOperation<I extends ComplexType, IU extends ComplexType, O extends ComplexType, OU extends ComplexType>
		extends HttpCall {

	private final Class<? extends O> wrappedOutputClass;
	private String soapAction;

	public WrappedOperation(Class<? extends O> wrappedOutputClass) {
		this(wrappedOutputClass, null, null);
	}

	public WrappedOperation(Class<? extends O> wrappedOutputClass, String defaultSoapAction) {
		this(wrappedOutputClass, defaultSoapAction, null);
	}

	public WrappedOperation(Class<? extends O> wrappedOutputClass, String defaultSoapAction, String defaultURI) {
		super();
		this.wrappedOutputClass = wrappedOutputClass;
		this.soapAction = defaultSoapAction;
		if (defaultURI != null) {
			try {
				setTarget(defaultURI);
			} catch (URISyntaxException e) {
				throw new WseException("Invalid default URI specified: '" + defaultURI + "'", e);
			}
		}
	}

	protected abstract I wrapInput(IU input);

	protected abstract OU unwrapOutput(O output);

	public OU call(IU input) throws WseException {
		try {
			O o = wrappedOutputClass.newInstance();
			HttpUtils.sendReceive(this, wrapInput(input), o);
			return unwrapOutput(o);
		} catch (WseException e) {
			throw e;
		} catch (Exception e) {
			throw new WseException(e);
		}
	}

	public void callAsync(final IU input, final HttpCall.AsyncCallback<OU> callback) {
		callAsync(input, new Consumer<OperationResult<OU>>() {
			@Override
			public void consume(OperationResult<OU> result) {
				if (result.getCause() != null) {
					callback.onFail(result.getCause());
				} else {
					callback.onSuccess(result.getResult());
				}
			}
		});
	}

	public void callAsync(final IU input, final Consumer<OperationResult<OU>> callback) {
		Runnable doAsync = new Runnable() {
			@Override
			public void run() {
				try {
					OU result = call(input);
					callback.consume(new OperationResult<OU>(result));
				} catch (Throwable e) {
					callback.consume(new OperationResult<OU>(e));
				}
			}
		};

		Thread inBackground = new Thread(doAsync, getServiceName() + "Async");
		inBackground.start();
	}

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

	@Override
	protected String getServiceName() {
		return this.getClass().getSimpleName();
	}

	public abstract class AsyncOperationCallback extends HttpCall.AsyncCallback<OU> {
	}
}
