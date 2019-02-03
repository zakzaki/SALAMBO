package fr.sorbonne_u.datacenter.software.admissioncontroller.utils;

import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;

/**
 * The class <code>Task</code> represents a task to be run on
 * AdmissionController
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * The task indicate the number of ApplicationVM , number of core ,
 * InboundApplication URI RequestSubmissionInboundPort of the RequestDispatcher
 * , Application URI and InboundApplication URI
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant	taskURI != null and request != null
 * </pre>
 * 
 * <p>
 * Created on : April 9, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * 
 */

public class ControllerTask implements RequestcontrolleurI {
	private static final long serialVersionUID = 1L;
	protected int numberofVM;
	protected int numberofcore;
	protected String notificationinboundportURI;
	protected String submissiondispatcherRIinbound;
	protected String applicationuri;
	protected String inboundapplicationURI;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * create a task object.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	request != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param numberVM
	 * @param numcores
	 * @param notificationinboundportURI
	 * @param applicationuri
	 * @param inboundapplicationURI
	 */
	public ControllerTask(int numberVM, int numcores, String notificationinboundportURI, String applicationuri,
			String inboundapplicationURI

	) {
		super();

		this.numberofcore = numcores;
		this.numberofVM = numberVM;
		this.notificationinboundportURI = notificationinboundportURI;
		this.applicationuri = applicationuri;
		this.inboundapplicationURI = inboundapplicationURI;

	}

	/**
	 * 
	 * @param submissiondispatcherRIinbound
	 */
	public ControllerTask(String submissiondispatcherRIinbound) {
		super();

		this.submissiondispatcherRIinbound = submissiondispatcherRIinbound;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	@Override
	public Integer getCoresnum() {
		// TODO Auto-generated method stub
		return this.numberofcore;
	}

	@Override
	public Integer getVMnum() {
		// TODO Auto-generated method stub
		return this.numberofVM;
	}

	@Override
	public String getnotificationinboundportURI() {
		return this.notificationinboundportURI;
	}

	@Override
	public String getsubmissionDispatcherURIinbound() {
		return this.submissiondispatcherRIinbound;

	}

	@Override
	public String getapplicationuri() {
		return this.applicationuri;
	}

	@Override
	public String getinboundappuri() {
		return this.inboundapplicationURI;
	}
}
