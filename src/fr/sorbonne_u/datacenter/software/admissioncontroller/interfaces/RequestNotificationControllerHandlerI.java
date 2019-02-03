package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

/**
 * The interface <code>RequestNotificationControllerHandlerI</code> defines the
 * methods that must be implemented by a component to handle request
 * notifications received through an inboud port
 * 
 * <code>RequestNotificationControllerHandlerI</code>.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * Created on : May 4, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public interface RequestNotificationControllerHandlerI {

	/**
	 * process the termination notification of a request.
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
	 *            terminated request.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void acceptRequestTerminationNotification(RequestcontrolleurI r) throws Exception;
}
