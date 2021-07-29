package wse.utils;

public class Operation<I extends ComplexType, O extends ComplexType> extends WrappedOperation<I, I, O, O>{
	
	public Operation(Class<? extends O> wrappedOutputClass,
			String defaultSoapAction, String defaultURI) {
		super(wrappedOutputClass, defaultSoapAction, defaultURI);
	}

	public Operation(Class<? extends O> wrappedOutputClass,
			String defaultSoapAction) {
		super(wrappedOutputClass, defaultSoapAction);
	}

	public Operation(Class<? extends O> wrappedOutputClass) {
		super(wrappedOutputClass);
	}

	@Override
	protected final I wrapInput(I input) {
		return input;
	}
	
	@Override
	protected final O unwrapOutput(O output) {
		return output;
	}

	
}
