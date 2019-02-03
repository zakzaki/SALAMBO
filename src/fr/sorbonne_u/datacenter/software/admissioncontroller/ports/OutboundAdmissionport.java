package fr.sorbonne_u.datacenter.software.admissioncontroller.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;


/**
 * The class <code>OutboundAdmissionport</code> implements the inbound port
 * requiring the interface <code>RequestSubmissionControllerI</code>.
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
 * <p>
 * Created on : April 9, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class OutboundAdmissionport extends AbstractOutboundPort implements RequestSubmissionControllerI {
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	owner instanceof RequestSubmissionControllerI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public OutboundAdmissionport(ComponentI owner) throws Exception {
		super(RequestSubmissionControllerI.class, owner);
	}

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	owner instanceof RequestSubmissionControllerI
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param uri
	 *            OutboundAdmissionport
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public OutboundAdmissionport(String uri, ComponentI owner) throws Exception {
		super(uri, RequestSubmissionControllerI.class, owner);

		assert uri != null;
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequest(final RequestcontrolleurI r) throws Exception {
		((RequestSubmissionControllerI) this.connector).submitRequest(r);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequestAndNotify(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */

	public void submitRequestAndNotify(RequestcontrolleurI r) throws Exception {
		((RequestSubmissionControllerI) this.connector).submitRequestAndNotify(r);
	}
}
