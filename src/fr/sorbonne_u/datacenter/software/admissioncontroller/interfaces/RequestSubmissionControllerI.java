package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

/**
 * The interface <code>RequestSubmissionControllerI</code> defines the component
 * services to receive and execute requests.
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
 * invariant	true
 * </pre>
 * 
 *
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public interface RequestSubmissionControllerI extends OfferedI, RequiredI {
	/**
	 * submit a request to a requestControlleur handler.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * 
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            request to be submitted.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public void submitRequest(final RequestcontrolleurI r) throws Exception;

	/**
	 * submit a request to a request handler and require notifications of request
	 * execution progress.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * 
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            request to be submitted.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public void submitRequestAndNotify(final RequestcontrolleurI r) throws Exception;
}
