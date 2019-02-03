package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

import java.io.Serializable;

/**
 * The interface <code>RequestcontrolleurI</code> must be implemented by requests submitted
 * to a <code>RequestcontrolleurI</code> for execution as a job for an
 * application running on the data center.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * the requests are sent to the admission controller to check the resources in
 * the data center
 * 
 * 
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public interface RequestcontrolleurI extends Serializable {
	/**
	 * return the number of cores
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */

	public Integer getCoresnum();

	/**
	 * return the number of VM.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */
	public Integer getVMnum();

	/**
	 * return the URI of inboundApplicationport.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */
	public String getnotificationinboundportURI();

	/**
	 * return the URI of submissionDispatcherinboudport.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */
	public String getsubmissionDispatcherURIinbound();

	/**
	 * return the URI of the application
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */
	public String getapplicationuri();

	/**
	 * return the URI of the inboundapplicationURI
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return the URI of the request.
	 */
	public String getinboundappuri();

}
