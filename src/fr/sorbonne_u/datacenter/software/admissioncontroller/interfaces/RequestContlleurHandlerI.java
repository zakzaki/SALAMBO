package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

/**
 * The interface <code>RequestContlleurHandlerI</code> defines the component
 * internal services to receive and execute requests.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public interface RequestContlleurHandlerI {
	/**
	 * accept the request <code>r</code> and execute it.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            the request to be executed.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public void acceptRequestSubmission(final RequestcontrolleurI r) throws Exception;

	/**
	 * accept the request <code>r</code>, execute it and notify its termination.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            the request to be executed.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public void acceptRequestSubmissionAndNotify(final RequestcontrolleurI r) throws Exception;

	/**
	 * accept the request termination notification <code>r</code>, execute it and
	 * notify its termination.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            the request to be executed.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public void acceptRequestTerminationNotification(final RequestcontrolleurI r);
}
