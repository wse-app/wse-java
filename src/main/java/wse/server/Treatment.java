package wse.server;

import wse.utils.exception.WseException;

public class Treatment {

	private HttpCallTreatment treatment;
	private Class<? extends HttpCallTreatment> clazz;

	public String getTreatmentClassName() {
		if (treatment != null)
			return treatment.getClass().getName();
		if (clazz != null)
			return clazz.getSimpleName();
		return null;
	}

	public Treatment(HttpCallTreatment treatment) {
		this.treatment = treatment;
	}

	public Treatment(Class<? extends HttpCallTreatment> clazz) {
		this.clazz = clazz;
	}

	public HttpCallTreatment getCallTreatment() {
		if (this.treatment != null) {
			return treatment;
		}

		if (this.clazz != null) {
			try {
				return clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new WseException("Could not create a new CallTreatment: " + e.getMessage(), e);
			}
		}
		return null;
	}
}