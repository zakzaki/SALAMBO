package fr.sorbonne_u.datacenter.software.application;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.connectors.RequestSubmissionControllerConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestNotificationControllerHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.InboundApplication;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.OutboundAdmissionport;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ControllerTask;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenterclient.requestgenerator.RequestGenerator;
import fr.sorbonne_u.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.sorbonne_u.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.sorbonne_u.datacenterclient.software.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

//-----------------------------------------------------------------------------
/**
 * The Application component is defined by the RequestGenerator component. In
 * order to deploy an application in the data center, the component must send a
 * request through its OutboundApplicationport indicating the number of
 * ApplicationVM and the number of Core needed for the deployment. If it
 * receives a notification from the AdmissionController, it retrieves the
 * RequestsubmissionDispatcher URI to connect it with the
 * RequestSubmissionOutboundPort RequestGenerator Port. At the end of this step,
 * the RequestGenerator connects with the Component and the Application is
 * deployed in the Data Center.
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
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class Application extends AbstractComponent implements RequestNotificationControllerHandlerI {
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------
	/** RequestGenerator URI */
	protected String RequestGeneratorURI;

	/** DynamicComponentCreationOutboundPort Port */

	protected DynamicComponentCreationOutboundPort porttoVM;

	/** Application URI */
	protected String ApplicationURI;

	/** URI Used by Reflection Port */

	protected String vmuri = "";

	/** RequestSubmissionOutboundPort URI */
	protected String requestSubmissionOutboundPortURI;

	/** Application OutboundAdmissionport Port */
	protected OutboundAdmissionport requestSubmissionOutboundControllerPort;
	/** mean inter-arrival time of the requests in ms. */
	protected Double meanInterArrivalTime;

	/** mean number of instructions of the requests in ms. */
	protected Long meanNumberOfInstructions;
	/** URI of the management RequestGenerator inbound port */
	protected String managementInboundPortURI;
	/** Application InboundApplication port */
	protected InboundApplication requestinboundApplication;
	/** ReflectionOutboundPort Port */
	protected ReflectionOutboundPort rop = new ReflectionOutboundPort(this);

	/** URI RequestGenerator RequestGeneratorManagementOutboundPort */
	protected String RequestGeneratorManagementOutboundPortURI;
	/** URI InboundApplication Port */
	protected String InboundApplicationURI;
	/** Number Of ApplicationVM */
	protected int numbervm;
	/** Number Of Core */
	protected int numbercores;
	/** RequestDispatcher URI RequestSubmissionInboundPort */
	protected String requestDispatcherSubmissionInboundPortURI;
	/** RequestGenerator RequestNotificationInboundPort URI */
	protected String requestDispatcherNotificationInboundPortURI;
	/** AdmissionController InboundAdmissioncontroller URI */
	protected String controllerinboundporturi;

	/**
	 * 
	 * the constructor takes as parameter the URI of the Application, a number of
	 * ApplicatioVM, a number of core, the RequestSubmissionOutboundPort URI, the
	 * mean interArrivalTime, mean numberofInstructions, the request generator's
	 * managementInboundPortURI and the InboundAdmissionport
	 * 
	 * @param AppUri
	 *            Application URI
	 * @param numbervm
	 *            Number of ApplicationVM
	 * @param numbercores
	 *            Number of Core
	 * @param meanInterArrivalTime
	 *            mean inter-arrival time of the requests in ms.
	 * @param meanNumberOfInstructions
	 *            mean number of instructions of the requests in ms.
	 * @param managementInboundPort
	 *            URI of the management inbound port.
	 * @param controllerinbounduri
	 *            URI of the controller InboundAdmissionport port.
	 * @throws Exception
	 */

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public Application(String AppUri, int numbervm, int numbercores, Double meanInterArrivalTime,
			Long meanNumberOfInstructions, String managementInboundPortURI, String controllerinbounduri)
			throws Exception {
		super(1, 0);
		this.ApplicationURI = AppUri;
		this.numbervm = numbervm;
		this.numbercores = numbercores;
		this.meanInterArrivalTime = meanInterArrivalTime;
		this.meanNumberOfInstructions = meanNumberOfInstructions;
		this.managementInboundPortURI = managementInboundPortURI;
		this.requestSubmissionOutboundPortURI = generaterequestSubmissionOutboundPortURI();
		this.RequestGeneratorManagementOutboundPortURI = generateRequestGeneratorManagementOutboundPortURI();
		this.RequestGeneratorURI = RequestGeneratorURI();
		this.addRequiredInterface(ComputerServicesI.class);

		this.addRequiredInterface(RequestSubmissionControllerI.class);

		this.requestSubmissionOutboundControllerPort = new OutboundAdmissionport(generateRequest(), this);
		this.addPort(this.requestSubmissionOutboundControllerPort);
		this.requestSubmissionOutboundControllerPort.publishPort();

		this.controllerinboundporturi = controllerinbounduri;

		this.InboundApplicationURI = generateinboudportapplication();
		this.addRequiredInterface(RequestNotificationControllerI.class);
		this.requestinboundApplication = new InboundApplication(InboundApplicationURI, this);
		this.addPort(this.requestinboundApplication);
		this.requestinboundApplication.publishPort();

	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void start() throws ComponentStartException {

		try {
			this.porttoVM = new DynamicComponentCreationOutboundPort(RequestportURI(), this);
			this.addPort(this.porttoVM);
			this.porttoVM.localPublishPort();
			this.porttoVM.doConnection(this.vmuri + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());

		} catch (Exception e) {
			throw new ComponentStartException(e);
		}

		// connection between the application and the admission controller through the
		// submissionoutboundController port by specifying the controller's
		// submissioninboundcontroller port uri
		try {
			this.doPortConnection(this.requestSubmissionOutboundControllerPort.getPortURI(),
					this.controllerinboundporturi, RequestSubmissionControllerConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

		} catch (Exception e1) {

			e1.printStackTrace();
		}

		super.start();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {
		if (this.porttoVM.connected()) {
			this.porttoVM.doDisconnection();
		}
		this.requestSubmissionOutboundControllerPort.doDisconnection();
		this.rop.doDisconnection();

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		try {
			this.porttoVM.unpublishPort();
			this.requestSubmissionOutboundControllerPort.unpublishPort();
			this.requestinboundApplication.unpublishPort();
			this.rop.unpublishPort();

		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void shutdownNow() throws ComponentShutdownException {
		try {
			this.porttoVM.unpublishPort();
			this.requestSubmissionOutboundControllerPort.unpublishPort();
			this.requestinboundApplication.unpublishPort();
			this.rop.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdownNow();
	}

	public String generateRgURI() {
		return this.ApplicationURI + "RG";
	}

	// -------------------------------------------------------------------------
	// Component services
	// -------------------------------------------------------------------------
	/**
	 * 
	 * The launch () method is used to create a ControllerTask request by specifying
	 * the number of vm, the number of core, the
	 * requestDispatcherNotificationInboundPortURI, the applicationURI, and the URI
	 * of the inboundapplication port. Once create the component send the request to
	 * the admission controller through the requestSubmissionOutboundController
	 * port,
	 * 
	 * @throws Exception
	 */

	public void launch() throws Exception {

		ControllerTask r = new ControllerTask(this.numbervm, this.numbercores,
				requestDispatcherNotificationInboundPortURI, ApplicationURI, this.InboundApplicationURI);
		try {
			this.requestSubmissionOutboundControllerPort.submitRequestAndNotify(r);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}

	}

	/**
	 * perform the creation and the connection of the components.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void dynamicDeploy() throws Exception {
		
		// RequestGenerator DynamicDeploy 
		
		
		this.requestDispatcherNotificationInboundPortURI = generaterequestDispatcherNotificationInboundPortURI();
		this.requestDispatcherSubmissionInboundPortURI = generaterequestdispatcherSubmissionInboundPortURI();
		try {
			this.porttoVM.createComponent(RequestGenerator.class.getCanonicalName(),
					new Object[] { RequestGeneratorURI, this.meanInterArrivalTime, this.meanNumberOfInstructions,
							this.managementInboundPortURI, this.requestDispatcherSubmissionInboundPortURI,
							this.requestSubmissionOutboundPortURI, this.requestDispatcherNotificationInboundPortURI,
							this.requestSubmissionOutboundPortURI });

		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			rop.doConnection(RequestGeneratorURI, ReflectionConnector.class.getCanonicalName());
		} catch (Exception e) {
			System.out.println(e);

		}

		rop.toggleTracing();
		rop.toggleLogging();

		/*
		 * rop.doDisconnection() ; rop.unpublishPort() ;
		 * this.removeRequiredInterface(ReflectionI.class) ; rop.destroyPort() ;
		 */

	}

	/**
	 * the acceptRequestterminationNotification method receives the admission
	 * controller notification containing the uri of the RequestDispatcherSubmission
	 * port that will be used to send the requests. Once the connection is complete,
	 * the request generator starts the query generation
	 */
	@Override
	public void acceptRequestTerminationNotification(final RequestcontrolleurI r) throws Exception {

		if (r.getsubmissionDispatcherURIinbound() != null) {

			// Connect RequestGenerator RequestSubmissionOutboundPort with RequestDispatcher
			// RequestSubmissionInBoundPort
			
			try {
				rop.doPortConnection(this.requestSubmissionOutboundPortURI, r.getsubmissionDispatcherURIinbound(),
						RequestSubmissionConnector.class.getCanonicalName());
			} catch (Exception e) {
				throw new ComponentStartException(e);
			}

			RequestGeneratorManagementOutboundPort rmop;
			this.addRequiredInterface(RequestGeneratorManagementI.class);
			rmop = new RequestGeneratorManagementOutboundPort(RequestGeneratorManagementOutboundPortURI, this);
			addPort(rmop);
			rmop.publishPort();

			try {
				doPortConnection(rmop.getPortURI(), this.managementInboundPortURI,
						RequestGeneratorManagementConnector.class.getCanonicalName());

			} catch (Exception e) {
				throw new ComponentStartException(e);
			}

			// Start RequestGeneration
			
			rmop.startGeneration();

		}

		super.start();
	}

	/** Generate URI */

	public String generaterequestdispatcherSubmissionInboundPortURI() {
		return this.ApplicationURI + "rdsbip";
	}

	/** Generate URI */
	public String generaterequestDispatcherNotificationInboundPortURI() {
		return this.ApplicationURI + "rdninp";
	}

	/** Generate URI */
	public String generateRequestGeneratorManagementOutboundPortURI() {
		return this.ApplicationURI + "genmanop";
	}

	/** Generate URI */
	public String RequestGeneratorURI() {
		return this.ApplicationURI + "Generator";
	}

	/** Generate URI */
	public String RequestportURI() {
		return this.ApplicationURI + "portdyn";
	}

	/** Generate URI */
	public String URI() {
		return this.ApplicationURI + "vmuri";
	}

	/** Generate URI */
	public String generateRequest() {
		return this.ApplicationURI + "rsocbp";
	}

	/** Generate URI */
	public String generateinboudportapplication() {
		return this.ApplicationURI + "inapplication";
	}

	/** Generate URI */
	public String generaterequestSubmissionOutboundPortURI() {
		return this.ApplicationURI + "rsobpuri";
	}

}

// -----------------------------------------------------------------------------
