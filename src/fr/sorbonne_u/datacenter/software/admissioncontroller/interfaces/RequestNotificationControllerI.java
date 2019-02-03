package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

/**
 * The interface <code>RequestNotificationControllerI</code> defines the
 * notification of request termination.
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

 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public interface RequestNotificationControllerI extends OfferedI, RequiredI {

	/**
	 * notify the termination of the request <code>r</code>.
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
	 *            the request which termination is notified.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void notifyRequestTermination(RequestcontrolleurI r) throws Exception;
}
