package fr.sorbonne_u.datacenter.software.admissioncontroller.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestNotificationControllerHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;


/**
 * The class <code>InboundApplication</code> implements the inbound port
 * offering the interface <code>InboundApplication</code>.
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
 * invariant	uri != null and owner instanceof RequestNotificationControllerHandlerI
 * </pre>
 * 
 * <p>
 * Created on : April 9, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class InboundApplication extends AbstractInboundPort implements RequestNotificationControllerI {
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
	 * pre	owner instanceof RequestNotificationControllerHandlerI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public InboundApplication(ComponentI owner) throws Exception {
		super(RequestNotificationControllerI.class, owner);

		assert owner instanceof RequestNotificationControllerHandlerI;
		assert uri != null;
	}

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	owner instanceof RequestNotificationControllerHandlerI
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param uri
	 *            InboundApplication URI
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public InboundApplication(String uri, ComponentI owner) throws Exception {
		super(uri, RequestNotificationControllerI.class, owner);

		assert uri != null && owner instanceof RequestNotificationControllerHandlerI;
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI#notifyRequestTermination(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */

	@Override
	public void notifyRequestTermination(final RequestcontrolleurI r) throws Exception {

		this.getOwner().handleRequestAsync(new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {
				((RequestNotificationControllerHandlerI) this.getOwner()).acceptRequestTerminationNotification(r);
				return null;
			}
		});
	}
}
