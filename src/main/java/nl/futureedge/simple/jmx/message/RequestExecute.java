package nl.futureedge.simple.jmx.message;

import java.util.Arrays;

/**
 * Request to execute an operation on the remote mbean server.
 */
public final class RequestExecute extends Request {

	private static final long serialVersionUID = 1L;

	private final String methodName;
	private final Class<?>[] parameterClasses;
	private final Object[] parameterValues;

	/**
	 * Constructor.
	 * 
	 * @param methodName
	 *            method name
	 * @param parameterClasses
	 *            parameter classes
	 * @param parameterValues
	 *            parameter values
	 */
	public RequestExecute(final String methodName, final Class<?>[] parameterClasses, final Object[] parameterValues) {
		this.methodName = methodName;
		if (parameterClasses == null) {
			this.parameterClasses = null;
		} else {
			this.parameterClasses = new Class<?>[parameterClasses.length];
			System.arraycopy(parameterClasses, 0, this.parameterClasses, 0, this.parameterClasses.length);
		}
		if (parameterValues == null) {
			this.parameterValues = null;
		} else {
			this.parameterValues = new Object[parameterValues.length];
			System.arraycopy(parameterValues, 0, this.parameterValues, 0, this.parameterValues.length);
		}
	}

	public String getMethodName() {
		return methodName;
	}

	public Class<?>[] getParameterClasses() {
		if (parameterClasses == null) {
			return new Class<?>[] {};
		}
		final Class<?>[] result = new Class<?>[parameterClasses.length];
		System.arraycopy(parameterClasses, 0, result, 0, parameterClasses.length);
		return result;
	}

	public Object[] getParameterValues() {
		if (parameterValues == null) {
			return new Object[] {};
		}
		final Object[] result = new Object[parameterValues.length];
		System.arraycopy(parameterValues, 0, result, 0, parameterValues.length);
		return result;
	}

	@Override
	public String toString() {
		return "RequestExecute [requestId=" + getRequestId() + ", methodName=" + methodName + ", parameterClasses="
				+ Arrays.toString(parameterClasses) + "]";
	}

}
