package fr.sorbonne_u.datacenter.software.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.datacenter.TimeManagement;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataOfferedI;
import fr.sorbonne_u.datacenter.interfaces.PushModeControllingI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.connectors.AdmissionControllerNotificationConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.connectors.RequestNotificationControllerConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerNotificationI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestContlleurHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.InboundAdmissionport;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.OutboundApplicationport;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationInformation;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ControllerTask;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.RingNetwork;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.sorbonne_u.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.connectors.RequestNotificationConnector;
import fr.sorbonne_u.datacenter.software.controller.PerformanceController;
import fr.sorbonne_u.datacenter.software.controller.PerformanceController.decision;
import fr.sorbonne_u.datacenter.software.controller.PerformanceController.strategy;
import fr.sorbonne_u.datacenter.software.controller.connectors.RingnetworkCoordinatorConnector;
import fr.sorbonne_u.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.sorbonne_u.datacenter.software.controller.interfaces.RingnetworkI;
import fr.sorbonne_u.datacenter.software.controller.ports.PerformanceControllerManagementInboundPort;
import fr.sorbonne_u.datacenter.software.controller.ports.RingnetworkCoordinatorInboundPort;
import fr.sorbonne_u.datacenter.software.controller.ports.RingnetworkCoordinatorOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ApplicationDynamicState;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.RequestDispatcher;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.RdDynamicStateDataInboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.sorbonne_u.datacenterclient.tests.Integrator;
import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;
import fr.sorbonne_u.datacenterclient.utils.AveragePerformanceChart;

/**
 * The class <code>AdmissionController</code> deploys an AdmissionController for
 * an application. This component decide to accept application deployment or
 * decline.
 *
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 *
 * <p>
 * This Component is used to verify Resources in the Data center and allocate
 * Core for an Application. AdmissionController receive an Application request
 * indicating the number of ApplicationVM and the number of necessary core to
 * launch it. AdmissionController search into the cores structure for a number
 * of cores into a Computer to run the Application.it return the URI of Computer
 * to allocate cores from the indicated one then deploy dynamically
 * ApplicationVMs and RequestDispatcher and interconnect them. After this step ,
 * AdmissionController send a notify to the application and indicate the
 * inboundDispatcherSubmission to connect the RequestGenerator with the
 * requestDispatcher. AdmissionController create on-demand applications and
 * create a Ring Network to share applicationVM with PerformanceController .
 *
 * </p>
 *
 *
 * <p>
 * <strong>Invariant</strong>
 * </p>
 *
 * <pre>
 * invariant		true
 * </pre>
 *
 * <p>
 * Created on : Jan, 2019
 * </p>
 *
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class AdmissionController extends AbstractComponent implements RequestContlleurHandlerI, PushModeControllingI,
		AdmissionControllerNotificationI, RingnetworkI, ControllerManagementI {

	/** URI of this AdmissionController */
	protected static String AdmissionControllerURI = "AdmissionController";

	/** DynamicComponentCreationOutboundPort Port */
	protected DynamicComponentCreationOutboundPort portdynamicComponentCreation;

	/** URI Used for Reflection port. */
	protected String admissionControllerUri = "";

	/** ArrayList to Save Allocated Core */
	protected ArrayList<AllocatedCore[]> alloc;

	/** URI of Computer and ComputerServicesInboundPort */
	protected Hashtable<String, String> Computeruriservice;

	/** List of this InboundAdmissionport of AdmissionController */
	protected ArrayList<InboundAdmissionport> portadmission;

	/** URI of Application. */
	protected String applicationuri;

	/**
	 * URI of InboundApplication and AdmissionController OutboundApplicationPort to
	 * connect with Application
	 */
	protected Integer compteur = 0;
	protected Hashtable<String, OutboundApplicationport> portoutboundapplication;

	/** Object used to synchronize Application Admission Request */
	protected static Object key = new Object();

	/**
	 * ApplicationVM information HashMap (ApplicationVMURI ,
	 * ApplicationVMinformation )
	 */

	protected HashMap<String, ApplicationVMinformation> AppVM;

	/**
	 * Applications Information HashMap (ApplicationURI , ApplicationInformation )
	 */

	protected HashMap<String, ApplicationInformation> ApplicationInformations;

	/** Port used to receive Data */

	protected RdDynamicStateDataInboundPort rdDynamicStateDataInboundPort;

	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;

	/** Reflection Port */

	protected ReflectionOutboundPort rop;

	/** ArrayList for PerformanceControllerManagementInboundPort */

	protected ArrayList<PerformanceControllerManagementInboundPort> AdmissionControllerManagementInboundPortlist;

	/** Notification Port used to send notification to PerformanceController */

	protected AdmissionControllerNotificationInboundPort admissionControllerNotifInboundPort;

	/** RingNetworkInboundPort used to connect to the RingNetwork */

	protected RingnetworkCoordinatorInboundPort ringnetwordinboundport;

	/** RingNetworkOutboundPort used to connect to the RingNetwork */

	protected RingnetworkCoordinatorOutboundPort ringnetworkoutboundport;

	/** RingNetwork needed to connect new PerformanceController in the Network */

	protected RingNetwork ringnetwork;

	/** ApplicationVM circulating in the RingNetwork */

	protected ArrayList<ApplicationVMPortsInformation> RingapplicationVM;

	/** Time used to check last RingNetwork Update */

	protected double timeUpdateRing;

	/** Number used to create next ApplicationVM in the Ring */

	protected Integer numberVMRING = 0;

	/** PerformanceControllerManagementInboundPort */

	protected PerformanceControllerManagementInboundPort AdmissionControllerManagementInboundPort;

	/** Number of cores used to create ApplicationVM */

	protected Integer numberOfCores = 2;
	
	protected HashMap<strategy, decision> controlStatus;
	
	protected Double lowerbound ; 
	
	protected Double upperbound ; 


	/**
	 *
	 * The Constructor of the AdmissionController
	 *
	 * @param cores
	 *            HashMap containing the URI of Computer as a key and number of Core
	 *            as a value
	 * @param portoutboundapplication
	 *            URI of OutboundApplicationport
	 * @param portInboundController
	 *            URI of InboundAdmissionport
	 * @param computerserviceportinboud
	 *            URI of ComputerServicesInboundPort
	 * @throws Exception
	 */

	public AdmissionController(Hashtable<String, Integer> cores, ArrayList<String> portoutboundapplication,
			ArrayList<String> portInboundController, Hashtable<String, String> computerserviceportinboud , Double lowerbound , Double upperbound , 
			HashMap<strategy, decision> controlStatus)
			throws Exception {

		super("AdmissionController", 2, 2);

		this.AppVM = new HashMap<String, ApplicationVMinformation>();
		this.Computeruriservice = computerserviceportinboud;
		this.addOfferedInterface(RequestSubmissionControllerI.class);
		this.portadmission = new ArrayList<InboundAdmissionport>();

		this.AdmissionControllerManagementInboundPortlist = new ArrayList<PerformanceControllerManagementInboundPort>();

		for (int i = 0; i < portInboundController.size(); i++) {
			InboundAdmissionport iba;
			iba = new InboundAdmissionport(portInboundController.get(i), this);
			addPort(iba);
			iba.publishPort();
			this.portadmission.add(iba);
		}
		this.addOfferedInterface(RequestNotificationControllerI.class);
		this.portoutboundapplication = new Hashtable<String, OutboundApplicationport>();
		for (int i = 0; i < portoutboundapplication.size(); i++) {
			this.portoutboundapplication.put(portoutboundapplication.get(i) + "portout",
					new OutboundApplicationport(portoutboundapplication.get(i) + "portout", this));
			this.addPort(this.portoutboundapplication.get(portoutboundapplication.get(i) + "portout"));
			this.portoutboundapplication.get(portoutboundapplication.get(i) + "portout").publishPort();
		}

		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.rdDynamicStateDataInboundPort = new RdDynamicStateDataInboundPort("-rddynamic", this);
		this.addPort(rdDynamicStateDataInboundPort);
		this.rdDynamicStateDataInboundPort.publishPort();

		this.ApplicationInformations = new HashMap<String, ApplicationInformation>();
		this.rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();

		this.admissionControllerNotifInboundPort = new AdmissionControllerNotificationInboundPort("-acnoinp", this);
		this.addPort(admissionControllerNotifInboundPort);
		this.admissionControllerNotifInboundPort.publishPort();

		this.ringnetwordinboundport = new RingnetworkCoordinatorInboundPort("-admissionRingin", this);
		this.addPort(ringnetwordinboundport);
		this.ringnetwordinboundport.publishPort();

		this.ringnetworkoutboundport = new RingnetworkCoordinatorOutboundPort("-admissionRingout", this);
		this.addPort(ringnetworkoutboundport);
		this.ringnetworkoutboundport.publishPort();

		this.ringnetwork = new RingNetwork();
		this.ringnetwork.setControllerAdmissionRingNetworkinboundportURI("-admissionRingin");
		this.ringnetwork.setLastOutboundportURI("-admissionRingout");
		this.RingapplicationVM = new ArrayList<ApplicationVMPortsInformation>();

		this.addOfferedInterface(ControllerManagementI.class);
		AdmissionControllerManagementInboundPort = new PerformanceControllerManagementInboundPort("-admssionManagement",
				this);
		this.addPort(AdmissionControllerManagementInboundPort);
		AdmissionControllerManagementInboundPort.publishPort();
		
		this.lowerbound = lowerbound ;
		this.upperbound = upperbound ; 
		
		this.controlStatus = new HashMap<strategy,decision>(controlStatus);
		
		tracer.setRelativePosition(AveragePerformanceChart.positionX, AveragePerformanceChart.positionY);
		if(AveragePerformanceChart.positionY==3) {
			AveragePerformanceChart.positionX ++ ;
			AveragePerformanceChart.positionY = 0 ; 
		}
		else AveragePerformanceChart.positionY ++ ;
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	public void start() throws ComponentStartException {
		try {
			this.portdynamicComponentCreation = new DynamicComponentCreationOutboundPort(this);
			this.addPort(this.portdynamicComponentCreation);
			this.portdynamicComponentCreation.localPublishPort();
			this.portdynamicComponentCreation.doConnection(
					this.admissionControllerUri + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
		this.toggleTracing();
		this.toggleLogging();

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {

		try {
			this.doPortDisconnection(portdynamicComponentCreation.getPortURI());
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		// Disconnect ports to the request emitter and to the processors owning
		// the allocated cores.
		try {
			for (int i = 0; i < portadmission.size(); i++) {
				portadmission.get(i).unpublishPort();
			}

			for (Map.Entry mapentry : portoutboundapplication.entrySet()) {
				((OutboundApplicationport) mapentry.getValue()).unpublishPort();
			}

			portdynamicComponentCreation.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException("processor services outbound port disconnection" + " error", e);
		}

		super.shutdown();
	}

	/**
	 *
	 * This method makes it possible to check the number of cores of each computer.
	 * this method provides updated data for use in the allocation of the
	 * applicationVM
	 *
	 *
	 * @return number of core per Computer
	 */

	public Hashtable<String, Integer> updateCoreinformation() throws Exception {
		Hashtable<String, Integer> core = new Hashtable<String, Integer>();

		// Connect to each Computer

		for (Entry<String, String> entry : Computeruriservice.entrySet()) {
			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();

			try {
				this.doPortConnection(csop.getPortURI(), entry.getValue(),
						ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Get Available number of Core

			core.put(entry.getKey(), csop.getAvailableCoresnumber());
		}
		return core;
	}

	/**
	 * The acceptRequestSubmissionAndnotify method receives a request from the
	 * Application to check the resources needed for the deployment.
	 * AcceptRequestSubmissionAndNotify recovers the number of Core, the number of
	 * ApplicationVM, the Application URI, the InboundApplication port URI, and the
	 * RequestGenerator RequestNotificationInboundPort port URI of the request.This
	 * method looks for resources by calling the SearchRessouces method and passing
	 * it as a parameter the number of Core and the number of ApplicationVM. If
	 * there is a Computer available, the method searchRessources returns the URI of
	 * the Computer which will be used for the reservation of resources.
	 * Getressource method which takes in parameter the Computer URI and the number
	 * of core necessary for the execution of the application . the uri of the
	 * Computer is used to connect to the computer and get tables of cores which
	 * will be passed to the ApplicationVM, at the end the AdmissionController call
	 * the DynamicDeploy method for Dynamic Component creation.
	 */

	public void acceptRequestSubmissionAndNotify(final RequestcontrolleurI r) throws Exception {
		//
		ArrayList<String> ComputerURIlist = new ArrayList<String>();
		synchronized (key) {

			alloc = new ArrayList<AllocatedCore[]>();
			ArrayList<ApplicationVMinformation> appvminfo = new ArrayList<ApplicationVMinformation>();
			String computerURIi = null;

			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();

			// Informations from the request

			int numbercores = r.getCoresnum();
			int numbervm = r.getVMnum();
			this.applicationuri = r.getapplicationuri();
			String inboundapplicationURI = r.getinboundappuri();
			String notificationinboundDispatcherportURI = r.getnotificationinboundportURI();

			this.logMessage("*** Looking for ressources for Application " + this.applicationuri + "*** \n");
			this.logMessage("** Cores number " + r.getCoresnum() + "\n");
			this.logMessage("** nombreVM " + r.getVMnum() + "\n");

			// Looking for resources for Application

			ComputerURIlist = SearchRessouces(numbercores, numbervm);

			// the size of the ArrayList indicates whether there are resources available or
			// not

			if (ComputerURIlist.size() >= numbervm) {

				this.logMessage("--- Ressources found in Datacenter --- ");

				for (int j = 0; j < ComputerURIlist.size(); j++) {

					// Use Computer URI to get the ComputerServiceInboundPort URI

					computerURIi = ComputerURIlist.get(j);
					String csipuri = getComputerServicesInboundPortURI(computerURIi);

					// Allocate cores from each Computer URI from the List

					this.logMessage("---------------------------------------------------\n");
					try {
						this.doPortConnection(csop.getPortURI(), csipuri,
								ComputerServicesConnector.class.getCanonicalName());
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Save AllocatedCore[]

					alloc.add(csop.allocateCores(numbercores));

					// Update information about ApplicationVM that share the same Computer and the
					// same Processor

					csop.updateCoordinator(alloc.get(j), generateVMURI(j));

					// Save information about ApplicationVM

					ApplicationVMinformation vminfo = new ApplicationVMinformation(generateVMURI(j), alloc.get(j),
							computerURIi, this.applicationuri, csipuri, GenerateVMapplicationVMManagementipuri(j));
					AppVM.put(generateVMURI(j), vminfo);

					// ArrayList for Application ApplicationVM

					appvminfo.add(vminfo);

				}

				ApplicationInformation information = new ApplicationInformation(appvminfo);

				this.ApplicationInformations.put(this.applicationuri, information);

				// Dynamic deployment of components

				dynamicDeploy(numbercores, numbervm, notificationinboundDispatcherportURI, inboundapplicationURI,
						appvminfo);

			} else {
				this.logMessage("**** Cannot find resources ****");
			}
		}
	}

	/**
	 * The SearchResources method takes into account a number of cores and a number
	 * of vm and allows to check in the data structure cores if there is an
	 * available number of cores it returns the computer uri otherwise return null
	 *
	 * @param numbercores
	 * @param numbervm
	 * @return
	 * @throws Exception
	 */
	protected ArrayList<String> SearchRessouces(int numbercores, int numbervm) throws Exception {

		// Update of core number of each Computer

		boolean exist = false;
		ArrayList<String> computerURI = new ArrayList<String>();
		Hashtable<String, Integer> core = updateCoreinformation();
		String computerURIi = "";

		// Search for resources in the HashMap core (ComputerURI , Number of cores per
		// Computer )

		for (int i = 0; i < numbervm; i++) {
			for (Entry<String, Integer> entry : core.entrySet()) {
				String key = entry.getKey();
				int value = entry.getValue();
				if (value >= numbercores && exist == false) {
					computerURIi = key;
					value = value - numbercores;
					core.put(key, value);
					exist = true;
					computerURI.add(computerURIi);
				}
			}

			// Search resources for each Application VM

			computerURIi = "";
			exist = false;
		}

		// the List must be the same size as the number of ApplicationVM

		return computerURI;
	}

	/**
	 * The getComputerServiceInboundPortURI method takes the computer's URI and
	 * returns the URI of its portinboundservice
	 *
	 * @param OrdinateurURI
	 * @return
	 */

	protected String getComputerServicesInboundPortURI(String OrdinateurURI) {

		return Computeruriservice.get(OrdinateurURI);

	}

	/**
	 * The dynamicDeploy method dynamically generates virtual machines and request
	 * dispatcher. At the beginning it generates the uri of the ports necessary for
	 * the interconnection then it creates the vm and the request dispatcher and
	 * interconnects the components thanks to the port of reflection. At the end the
	 * dynamicDeploy creates an integrator that allows resources to be allocated to
	 * the VMs and sends a notification to the application through the
	 * outboundapplication port containing the RequestDispatcherSubmissionPortURI
	 * uri to link it with the request generator.
	 *
	 * @param numbercores
	 * @param numberVM
	 * @param notificationinboundportURI
	 * @param inboundport
	 * @throws Exception
	 */
	public void dynamicDeploy(int numbercores, int numberVM, String notificationinboundportURI, String inboundport,
			ArrayList<ApplicationVMinformation> appinfo) throws Exception {

		HashMap<String, ApplicationVMPortsInformation> applicationVMinformation = new HashMap<String, ApplicationVMPortsInformation>();
		HashMap<String, Integer> ApplicationVMManagementInboundPortURIList = new HashMap<String, Integer>();
		ArrayList<String> RequestDispatcherSubmissionURIlist = new ArrayList<String>();
		ArrayList<String> RequestDispatcherNotificationURIlist = new ArrayList<String>();

		// Connect to the application to send the notification of the request

		try {
			this.doPortConnection(this.applicationuri + "portout", inboundport,
					RequestNotificationControllerConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Creating ApplicationVM

		for (int i = 0; i < numberVM; i++) {

			// Generating URI for ApplicationVM

			String applicationVMURI = generateVMURI(i);
			String portManagement = GenerateVMapplicationVMManagementipuri(i);
			String portsubinbound = GenerateVMapplicationsubinboundporturi(i);
			String portnotificationinbound = GenerateVMapplicationnotificationinboundporturi(i);
			String portnotificationoutbound = GenerateVMapplicationnotificationoutboundporturi(i);

			ApplicationVMPortsInformation applicationPort = new ApplicationVMPortsInformation(applicationVMURI,
					portsubinbound, portnotificationoutbound, false, appinfo.get(i));
			applicationVMinformation.put(applicationVMURI, applicationPort);

			// Dynamic Creation of ApplicationVM

			try {
				this.portdynamicComponentCreation.createComponent(ApplicationVM.class.getCanonicalName(),
						new Object[] { applicationVMURI, portManagement, portsubinbound, portnotificationinbound,
								portnotificationoutbound, });
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Informations needed for RequestDispatcher to connect with ApplicationVM

			RequestDispatcherSubmissionURIlist.add(portsubinbound);
			RequestDispatcherNotificationURIlist.add(portnotificationinbound);

			// Information Used for integrator to allocate cores for ApplicationVM

			ApplicationVMManagementInboundPortURIList.put(portManagement, numbercores);

		}

		// Generating URI for RequestDispatcher

		String requestDispatcherManagementInboundURI = generateRDManagmentpuri();
		String RequestDispatcherURI = GenerateRequestDispatcherURI();
		String RequestDispatcherSubmissionInboundPortURI = GenerateDispatcherSubmissionInboundPortURI();
		String requestDispatcherNotificationOutboundPortURI = GeneraterequestDispatcherNotificationOutboundPortURI();

		// Dynamic Creation of RequestDispatcher

		try {
			this.portdynamicComponentCreation.createComponent(RequestDispatcher.class.getCanonicalName(),
					new Object[] { RequestDispatcherURI, this.applicationuri, RequestDispatcherSubmissionInboundPortURI,
							RequestDispatcherSubmissionURIlist, RequestDispatcherNotificationURIlist,
							notificationinboundportURI, // URI GEN
							requestDispatcherNotificationOutboundPortURI, requestDispatcherManagementInboundURI,
							applicationVMinformation });
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Using Reflection port to connect VM's NotificationOutboundPort to
		// RequestDispatcher NotificationInboundPort

		for (int i = 0; i < numberVM; i++) {

			String applicationVMURI = generateVMURI(i);
			try {
				rop.doConnection(applicationVMURI, ReflectionConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();

			}
			String portnotificationinbound = GenerateVMapplicationnotificationinboundporturi(i);
			String portnotificationoutbound = GenerateVMapplicationnotificationoutboundporturi(i);

			try {
				rop.doPortConnection(portnotificationoutbound, portnotificationinbound,
						RequestNotificationConnector.class.getCanonicalName());
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Using Reflection port to connect RequestDispatcher NotificationOutboundPort
		// to RequestGenerator NotificationInboundPort

		try {
			rop.doConnection(RequestDispatcherURI, ReflectionConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			rop.doPortConnection(requestDispatcherNotificationOutboundPortURI, notificationinboundportURI,
					RequestNotificationConnector.class.getCanonicalName());
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		// Dynamic Creation of Integrator

		String integratoruri = generateintegrator();

		try {
			this.portdynamicComponentCreation.createComponent(Integrator.class.getCanonicalName(),
					new Object[] { integratoruri, ApplicationVMManagementInboundPortURIList, this.alloc });
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Dynamic Creation of PerformanceController

		String ringinboundPortURI = this.applicationuri + "-ringinport";
		String ringOutboundPortURI = this.applicationuri + "-ringoutport";

	
		try {
			this.portdynamicComponentCreation.createComponent(PerformanceController.class.getCanonicalName(),
					new Object[] { this.applicationuri, this.applicationuri + "controller", RequestDispatcherURI,
							this.applicationuri + "-avgport", requestDispatcherManagementInboundURI, ringinboundPortURI,
							ringOutboundPortURI, numberVM , this.lowerbound , this.upperbound , this.controlStatus });
		} catch (Exception e) {
			e.printStackTrace();
		}

		rop.doConnection(this.applicationuri + "controller", ReflectionConnector.class.getCanonicalName());

		// Creation ApplicationVM for Ring Network

		try {
			allocateAppVM(numbercores);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Send Notification to the Application
		ControllerTask r = new ControllerTask(RequestDispatcherSubmissionInboundPortURI);
		this.portoutboundapplication.get(this.applicationuri + "portout").notifyRequestTermination(r);

		// Connect the PerformanceController in the Ring Network

		joinRingNetwork(ringinboundPortURI, ringOutboundPortURI, this.applicationuri + "controller");

	}

	public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
		return new ApplicationDynamicState(this.ApplicationInformations.get("app1"));
	}

	public void sendDynamicState() throws Exception {
		if (this.rdDynamicStateDataInboundPort.connected()) {
			RequestDispatcherDynamicStateI cds = this.getDynamicState();
			this.rdDynamicStateDataInboundPort.send(cds);
		}
	}

	/**
	 * push the dynamic state of the computer through its notification data inbound
	 * port at a specified time interval in ms and for a specified number of times.
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
	 * @param interval
	 *            time interval between data pushes.
	 * @param numberOfRemainingPushes
	 *            number of data pushes yet to be done.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void sendDynamicState(final int interval, int numberOfRemainingPushes) throws Exception {
		this.sendDynamicState();
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1;
		if (fNumberOfRemainingPushes > 0) {
			this.pushingFuture = this.scheduleTask(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((AdmissionController) this.getOwner()).sendDynamicState(interval, fNumberOfRemainingPushes);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, TimeManagement.acceleratedDelay(interval), TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * @see fr.sorbonne_u.datacenter.interfaces.PushModeControllingI#startUnlimitedPushing(int)
	 */

	public void startUnlimitedPushing(int interval) throws Exception {
		// first, send the static state if the corresponding port is connected

		this.pushingFuture = this.scheduleTaskAtFixedRate(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((AdmissionController) this.getOwner()).sendDynamicState();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, TimeManagement.acceleratedDelay(interval), TimeManagement.acceleratedDelay(interval), TimeUnit.MILLISECONDS);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.interfaces.PushModeControllingI#startLimitedPushing(int,
	 *      int)
	 */

	public void startLimitedPushing(final int interval, final int n) throws Exception {
		assert n > 0;

		this.logMessage(this.admissionControllerUri + " startLimitedPushing with interval " + interval + " ms for " + n
				+ " times.");

		// first, send the static state if the corresponding port is connected

		this.pushingFuture = this.scheduleTask(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((AdmissionController) this.getOwner()).sendDynamicState(interval, n);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, TimeManagement.acceleratedDelay(interval), TimeUnit.MILLISECONDS);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.interfaces.PushModeControllingI#stopPushing()
	 */

	public void stopPushing() throws Exception {
		if (this.pushingFuture != null && !(this.pushingFuture.isCancelled() || this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false);
		}
	}

	/**
	 * the method allows the creation of applicationVM for an Application . The
	 * performance controller try to intercept ApplicationVM circulating in the
	 * network . if it can not find any available applicationVM . The
	 * PerformanceController ask the AdmissionController to create VM . CreateVM
	 * method looks for resources in computers to create a new applicationVM. At the
	 * end, it send information about ApplicationVM to the RequestDispatcher to
	 * connect it in the Application
	 *
	 * @param applicationURI
	 *            The application URI of the request
	 * @param requestDispatcherInboundManagementURI
	 *            The requestDispatcherInboundManagementURI used to send
	 *            ApplicationVM to the RequestDispatcher
	 * @param numberVM
	 *            The number of the VM in the application used to generate URI's
	 * @throws Exception
	 */

	@Override
	public void CreateVM(String applicationURI, String requestDispatcherInboundManagementURI, int numberVM)
			throws Exception {

		// Connect to PerformanceController to send notification about ApplicationVM
		// creation state

		this.logMessage(
				"*************** Looking for Ressources for new ApplicationVM for applicationURI ******************"
						+ applicationURI);
		AdmissionControllerNotificationOutboundPort notificationOutboundPort = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(notificationOutboundPort);
		notificationOutboundPort.publishPort();

		boolean exist = false;
		String computerURIi = null;

		ComputerServicesOutboundPort csop;
		csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();

		// Update cores information from computers
		// Search for resources for Application

		for (Entry<String, Integer> entry : updateCoreinformation().entrySet()) {
			if (entry.getValue() > this.numberOfCores) {
				exist = true;
				computerURIi = entry.getKey();
				break;
			}

		}

		// Resources found in a computer

		if (computerURIi != null) {

			this.logMessage(" ***** Ressources found in Computer for the new ApplicationVM " + computerURIi + " *****");

			String csipuri = getComputerServicesInboundPortURI(computerURIi);

			// Connect to ComputerService to allocate Cores

			try {
				this.doPortConnection(csop.getPortURI(), csipuri, ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			alloc.add(csop.allocateCores(this.numberOfCores));

			// Generating URI for ApplicationVM
			String applicationVMURI = generateVMURI(numberVM);
			String portManagement = GenerateVMapplicationVMManagementipuri(numberVM);
			String portsubinbound = GenerateVMapplicationsubinboundporturi(numberVM);
			String portnotificationinbound = GenerateVMapplicationnotificationinboundporturi(numberVM);
			String portnotificationoutbound = GenerateVMapplicationnotificationoutboundporturi(numberVM);

			// Dynamic Creation Of ApplicationVM

			try {
				this.portdynamicComponentCreation.createComponent(ApplicationVM.class.getCanonicalName(),
						new Object[] { applicationVMURI, portManagement, portsubinbound, portnotificationinbound,
								portnotificationoutbound, });
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.addOfferedInterface(RequestDispatcherManagementOutboundPort.class);
			this.rop.doConnection(applicationVMURI, ReflectionConnector.class.getCanonicalName());

			
			// Update the Coordinator 
			
			csop.updateCoordinator(alloc.get(this.alloc.size()-1), applicationVMURI);
			
			// Connect to the RequestDispatcher ManagementInboundPort to connect the new
			// ApplicationVM
			
			

			RequestDispatcherManagementOutboundPort rmop = new RequestDispatcherManagementOutboundPort(this);
			this.addPort(rmop);
			rmop.publishPort();

			try {
				this.doPortConnection(rmop.getPortURI(), requestDispatcherInboundManagementURI,
						RequestDispatcherManagementConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Connect to the ApplicationVM ManagementInboundPort to send allocatedCores

			this.addOfferedInterface(ApplicationVMManagementI.class);
			ApplicationVMManagementOutboundPort avmop = new ApplicationVMManagementOutboundPort(this);
			this.addPort(avmop);
			avmop.publishPort();

			try {

				this.doPortConnection(avmop.getPortURI(), portManagement,
						ApplicationVMManagementConnector.class.getCanonicalName());

			} catch (Exception e) {
				e.printStackTrace();
			}

			// allocateCores to application VM
			avmop.allocateCores(alloc.get(alloc.size() - 1));

			// Add the new ApplicationVM information to AdmissionController List of AppVM
			// and send applicationVM information to RequestDispatcher

			ApplicationVMinformation vminfo = new ApplicationVMinformation(applicationVMURI,
					alloc.get(alloc.size() - 1), computerURIi, applicationURI, csipuri, portManagement);
			AppVM.put(applicationVMURI, vminfo);

			// Connect new ApplicationVM to RequestDispatcher

			rmop.ConnectVM(portsubinbound, applicationVMURI, portnotificationoutbound, vminfo);

		} else {

			this.logMessage("*** Cannot find ressources to create new ApplicationVM for *** " + applicationURI + "/n");
		}

		// Send Notification to Performance Controller

		try {
			this.doPortConnection(notificationOutboundPort.getPortURI(), applicationURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		notificationOutboundPort.addVMNotification(exist);
	}

	/**
	 * the method allows the creation of applications for Ring Network.
	 * allocateAppVM looks for resources in computers to create a new applicationVM.
	 * At the end, it adds the new VM to the list of VMs in the network
	 *
	 *
	 * @throws Exception
	 * @param numbercore
	 *            the number of core to allocate for ApplicationVM
	 */

	public void allocateAppVM(int numbercore) throws Exception {

		boolean exist = false;
		String computerURIi = null;

		//
		ComputerServicesOutboundPort csop;
		csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();

		// Update cores information from computers
		// Search for resources for Application

		for (Entry<String, Integer> entry : updateCoreinformation().entrySet()) {
			if (entry.getValue() >= numbercore) {
				exist = true;
				computerURIi = entry.getKey();
				break;
			}

		}

		// Resources found in a computer

		if (computerURIi != null) {
			this.logMessage(" ***** Ressources found in Computer for the new ApplicationVM *****");

			// Getting ComputerServiceportURI

			String csipuri = getComputerServicesInboundPortURI(computerURIi);

			// Connect to Computer to allocate cores
			try {
				this.doPortConnection(csop.getPortURI(), csipuri, ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			alloc.add(csop.allocateCores(numbercore));

			// Generating URI for the new ApplicationVM

			String applicationVMURI = "appVM-Ring" + this.numberVMRING;
			String portManagement = "-portManagement" + this.numberVMRING;
			String portsubinbound = "-portsubmissionin" + this.numberVMRING;
			String portnotificationinbound = "-portnotificationinbound" + this.numberVMRING;
			String portnotificationoutbound = "-portnotificationoutbound" + this.numberVMRING;

			// Dynamic Creation for ApplicationVM

			try {
				this.portdynamicComponentCreation.createComponent(ApplicationVM.class.getCanonicalName(),
						new Object[] { applicationVMURI, portManagement, portsubinbound, portnotificationinbound,
								portnotificationoutbound, });
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			csop.updateCoordinator(alloc.get(this.alloc.size()-1), applicationVMURI);

			// Save information for the new ApplicationVM

			ApplicationVMinformation vminfo = new ApplicationVMinformation(applicationVMURI,
					alloc.get(this.alloc.size() - 1), computerURIi, "", csipuri, portManagement);

			// Update the List of ApplicationVM for AdmissionController
			AppVM.put(applicationVMURI, vminfo);

			// Adding ApplicationVM Ports information in the Ring Network to share it with
			// PerfomranceControllers

			ApplicationVMPortsInformation appVMPortinformation = new ApplicationVMPortsInformation(applicationVMURI,
					portsubinbound, portnotificationoutbound, true, vminfo);

			this.RingapplicationVM.add(appVMPortinformation);

			// Connect to the ApplicationVM ManagementInboundPort to allocate cores

			this.addOfferedInterface(ApplicationVMManagementI.class);
			ApplicationVMManagementOutboundPort avmop = new ApplicationVMManagementOutboundPort(this);
			this.addPort(avmop);
			avmop.publishPort();

			try {

				this.doPortConnection(avmop.getPortURI(), portManagement,
						ApplicationVMManagementConnector.class.getCanonicalName());

			} catch (Exception e) {
				e.printStackTrace();
			}

			// Update the Coordinator 
			
			avmop.allocateCores(alloc.get(alloc.size() - 1));

			// Change the time used to update Ring network (this time is used to add or
			// remove new ApplicationVM to the Ring Network )

			this.timeUpdateRing = System.nanoTime() / (Double) 1_000_000_000.0;

			// Update the number of ApplicationVM in the ring (Used to generate next URI's)

			this.numberVMRING++;

		} else {
			this.logMessage("*** Cannot find ressources to create new ApplicationVM for *** ");
		}

	}

	/**
	 * checkRingNetwork method control the network. Every 10 seconds, the method
	 * checks whether the applicationVM is already used and checks whether the
	 * admissionController has not updated the VM list. In this case, this method
	 * allows to allocate a new applicationVM for the network. If the ApplicationVM
	 * circulate in the network after a definite time without being picked up by a
	 * PerformanceController , this method allows you to remove a VM and release its
	 * resources.
	 *
	 *
	 * @throws Exception
	 */

	private void checkRingNetwork() {
		this.scheduleTask(new AbstractComponent.AbstractTask() {

			@Override
			public void run() {

				try {

					// Check Ring Network

					if (isUpdateRing() && isUsedVMinRing()) {

						// If all ApplicationVM are used in the network and it's been a long time since
						// the last update ( Since we don't have a new ApplicationVM in the Ring ) , we
						// allocate new applicationVM for the Ring Network.

						allocateAppVM(numberOfCores);
					}
					if (isUnsedVMinRing()) {

						// If ApplicationVM are unused in the network and it's been a long time since
						// the last update , We can remove them to save resources and energy.

						removeVMFromRing();
					}

					checkRingNetwork();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}, 10000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Checking the Time after the last Ring Update
	 *
	 */

	public boolean isUpdateRing() {

		boolean isTimetoupdate = false;

		if ((System.nanoTime() / (Double) 1_000_000_000.0) - this.timeUpdateRing > 100) {
			isTimetoupdate = true;
		}

		return isTimetoupdate;
	}

	/**
	 * Checking if Ring ApplicatioVM are used or not
	 *
	 */
	public boolean isUsedVMinRing() {

		boolean isUsedVMinRing = true;

		for (int i = 0; i < this.RingapplicationVM.size(); i++) {
			if (this.RingapplicationVM.get(i).isFreeVM()) {
				isUsedVMinRing = false;
				break;
			}
		}
		return isUsedVMinRing;
	}

	/**
	 * Checking if Ring ApplicatioVM are unused after period of time
	 *
	 */

	public boolean isUnsedVMinRing() {

		boolean isunsedVMinRing = true;

		for (int i = 0; i < this.RingapplicationVM.size(); i++) {
			if (!this.RingapplicationVM.get(i).isFreeVM()) {
				isunsedVMinRing = false;
				break;
			}
		}
		if (isunsedVMinRing && isUpdateRing()) {
			return true;
		} else
			return false;
	}

	/**
	 * The receiveVM method receive ApplicationVM informations from the RingNetwok
	 * and verify if the real list of RingNetwork ApplicationVM is updated . In this
	 * case , it set the new information in the copy and update the original list .
	 * At the end it push the list in the RingNetwork.
	 * 
	 * @param ringappVM
	 *            Contain VM's informations circulating in the RingNetwork
	 * 
	 */

	public void receiveVM(ArrayList<ApplicationVMPortsInformation> oldRinginformation) throws Exception {

		// Thread sleep decrease the speed of data exchange

		Thread.sleep(100);

		// Getting copy of information circulating on the network

		ArrayList<ApplicationVMPortsInformation> newRinginformation = new ArrayList<ApplicationVMPortsInformation>(
				oldRinginformation);

		// Compare the size of List circulating on the network with the real list of
		// ApplicationVM

		// If the real list size is greater then the list circulating on the network ,
		// then the system has added a new ApplicationVM

		if (newRinginformation.size() < this.RingapplicationVM.size()) {

			// Update the List circulating on the network

			for (int i = this.RingapplicationVM.size() - 1; i > newRinginformation.size() - 1; i--) {

				newRinginformation.add(this.RingapplicationVM.get(i));

			}

			// Change the Real ring List with the new List circulating on the network (to
			// conserve state of VM )

			this.RingapplicationVM = new ArrayList<ApplicationVMPortsInformation>(newRinginformation);

		}

		// Pushing the new version in the Ring Network

		this.ringnetworkoutboundport.receiveVM(newRinginformation);

	}

	/**
	 * 
	 * this method is used to delete VM applications from the network. after a
	 * period of inactivity the admissionController can decide to free the resources
	 * and remove an ApplicationVM from the network
	 * 
	 * @throws Exception
	 */

	public void removeVMFromRing() throws Exception {

		// We can remove applicationVM from Ring Network to save energy and resources .
		// We can not remove all ApplicationVM From Ring

		if (this.RingapplicationVM.size() > 1) {

			String vmURI = this.RingapplicationVM.get(0).getApplicationVMURI();
			String ordinateurURIservice = this.AppVM.get(vmURI).getComputerserviceuri();
			AllocatedCore[] allocatedcore = this.AppVM.get(vmURI).getAllocatedcore();

			// Connect to the ComputerServiceInboundPort

			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();

			// Deallocate cores

			try {
				this.doPortConnection(csop.getPortURI(), ordinateurURIservice,
						ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			csop.DeallocateCores(allocatedcore,vmURI);

			// Update the time used to update the ring Network

			this.timeUpdateRing = System.nanoTime() / (Double) 1_000_000_000.0;
		}
	}

	/**
	 * 
	 * this method is used to allow interconnection of the nodes in the ring
	 * network. at the beginning the AdmissionController connects with the first
	 * PerformanceController. when adding a new AdmissionController it will be
	 * connected to the old controller and connects to the Inbound port of
	 * AdmissionController
	 * 
	 * @param ringinboundPort
	 * @param ringoutboundPort
	 * @param controllerURI
	 * @throws Exception
	 */

	public void joinRingNetwork(String ringinboundPort, String ringoutboundPort, String controllerURI)
			throws Exception {

		String LastOutboundPortURI = this.ringnetwork.getLastOutboundportURI();

		if (this.compteur == 0) {

			// For the first step (iteration ) , we connect the AdmissionController
			// RingNetworkOutboundPort with the first PerformanceController
			// RingNetworkInboundPort

			try {
				this.ringnetworkoutboundport.doConnection(ringinboundPort,
						RingnetworkCoordinatorConnector.class.getCanonicalName());

			}

			catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			// We connect the Last PerformanceController RingNetworkOutboundPort with the
			// new PerformanceController RingNetworkInboundPort

			rop.doConnection(this.ringnetwork.getLastControllerURI(), ReflectionConnector.class.getCanonicalName());

			try {
				rop.doPortConnection(LastOutboundPortURI, ringinboundPort,
						RingnetworkCoordinatorConnector.class.getCanonicalName());

			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// We Connect the New PerformanceControlle RingNetworkOutboundPort with the
		// AdmissionController RingNetworkInboundPort

		rop.doConnection(controllerURI, ReflectionConnector.class.getCanonicalName());

		String ControllerinboundPortURI = this.ringnetwork.getControllerAdmissionRingNetworkinboundportURI();

		try {
			rop.doPortConnection(ringoutboundPort, ControllerinboundPortURI,
					RingnetworkCoordinatorConnector.class.getCanonicalName());
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		// Update information in RingNetwork ( Set the Last RingNetworkOutboundPort and
		// the Last PerformanceController URI )

		this.ringnetwork.setLastOutboundportURI(ringoutboundPort);
		this.ringnetwork.setLastControllerURI(controllerURI);

		// After Connection of all PerformanceController , we can launch the first data

		// 1 = numberof application

		if (this.compteur == 1) {

			// Send data to the Ring Network and launch the network control
			try {
				this.timeUpdateRing = System.nanoTime() / (Double) 1_000_000_000.0;
				checkRingNetwork();
				this.ringnetworkoutboundport.receiveVM(this.RingapplicationVM);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		this.compteur++;

	}

	/**
	 * this method is used to add new ApplicationVM in the list of RingNetwork
	 * ApplicationVM . updateRingVM add the received from RequestDispatcher released
	 * ApplicationVM
	 * 
	 * @param appinfo
	 *            ApplicationVM port
	 */

	@Override
	public void updateRingVM(boolean exist, ApplicationVMPortsInformation appinfo) throws Exception {

		// Update the Ring ApplicationVM List

		this.RingapplicationVM.add(appinfo);
		this.timeUpdateRing = System.nanoTime() / (Double) 1_000_000_000.0;

	}

	/**
	 * generate methods generate Request Generator URI
	 */
	public String generateRDURI() {
		return this.applicationuri + "RD";
	}

	/**
	 * generate methods generate VM URI
	 */
	public String generateVMURI(int i) {
		return this.applicationuri + "VM" + i;

	}

	/**
	 * generate methods generate Integrator URI
	 */
	public String generateintegrator() {
		return this.applicationuri + "integrator";
	}

	/**
	 * generate methods generate VMapplicationVMManagement URI
	 */
	public String GenerateVMapplicationVMManagementipuri(int i) {
		return this.generateVMURI(i) + "vmmiuri";
	}

	/**
	 * generate methods generate URI
	 */
	public String GenerateVMapplicationsubinboundporturi(int i) {
		return this.generateVMURI(i) + "vmibp";
	}

	/**
	 * generate methods generate URI
	 */
	public String GenerateVMapplicationnotificationinboundporturi(int i) {
		return this.generateVMURI(i) + "vmnibp";
	}

	/**
	 * generate methods generate VMapplicationnotificationoutbound URI
	 */
	public String GenerateVMapplicationnotificationoutboundporturi(int i) {

		return this.generateVMURI(i) + "vmnobp";
	}

	/**
	 * generate methods generate RequestDispatcher URI
	 */
	public String GenerateRequestDispatcherURI() {
		return this.generateRDURI();
	}

	/**
	 * generate methods generate DispatcherSubmissionInboundPort URI
	 */
	public String GenerateDispatcherSubmissionInboundPortURI() {
		return this.applicationuri + "rdsinpuri";
	}

	/**
	 * generate methods generate DispatcherNotificationOutboundPort URI
	 */
	public String GeneraterequestDispatcherNotificationOutboundPortURI() {
		return this.applicationuri + "rnsinpuri";
	}

	public String generateRDManagmentpuri() {
		return this.applicationuri + "-rdmibp";
	}

	public String generateControlleruri() {
		return this.applicationuri + "Controller";
	}

	public String genereterequestDispatcherDynamicinboundport() {
		return this.applicationuri + "-avgport";
	}

	@Override
	public void frequencyIncreaseNotification(Boolean state) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void frequencyDecreaseNotification(Boolean state) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void removecoresNotification(boolean findressouce) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVMNotification(Boolean state) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeVMNotification(Boolean state) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void DestroyVM(String applicationURI, String appVM) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCores() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCores() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void IncreaseFrequencyControl() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void DecreaseFrequencyControl() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRequestSubmission(RequestcontrolleurI r) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRequestTerminationNotification(RequestcontrolleurI r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addcoresNotification(boolean findressouce) throws Exception {
		// TODO Auto-generated method stub

	}

}
