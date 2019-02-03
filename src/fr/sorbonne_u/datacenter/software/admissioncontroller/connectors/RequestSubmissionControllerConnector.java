package fr.sorbonne_u.datacenter.software.admissioncontroller.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;

/**
 * The class <code>RequestSubmissionControllerConnector</code> implements a
 * connector for ports exchanging through the interface
 * <code>RequestSubmissionControllerConnector</code>.
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
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class RequestSubmissionControllerConnector extends AbstractConnector implements RequestSubmissionControllerI {
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequest(RequestcontrolleurI r) throws Exception {
		((RequestSubmissionControllerI) this.offering).submitRequest(r);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequestAndNotify(RequestcontrolleurI r) throws Exception {

		((RequestSubmissionControllerI) this.offering).submitRequestAndNotify(r);
	}
}
