package fr.sorbonne_u.datacenterclient.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.datacenter.TimeManagement;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.interfaces.PushModeControllingI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.connectors.AdmissionControllerNotificationConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;
import fr.sorbonne_u.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.sorbonne_u.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.connectors.RequestNotificationConnector;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.sorbonne_u.datacenter.software.controller.ports.PerformanceControllerManagementInboundPort;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDynamicStateI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.AverageDynamicStateDataInboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;
import fr.sorbonne_u.datacenterclient.utils.AveragePerformanceChart;

/**
 * The class <code>RequestDispatcher</code> is a component that distributes
 * Request between the ApplicationVM using round Robin.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * The component receives requests from the generator through its
 * RequestSubmissionInboundPort port and in turn transmits the queries to the
 * ApplicationVM through the RequestSubmissionOutboundPort port connected with
 * the VM in question. At the end, it receives the notifications through its
 * RequestNotificationInboundPort from a VM and passes the notification to the
 * Generator through the RequestNotificationOutboundPort.
 * 
 * the RequestDispatcher is used to calculate the average execution of the
 * application that will be sent to the Performance controller to perform checks
 * to stabilize the execution time of the application. requestDispatcher can
 * connect and disconnect an applicationVM. it can also increase or decrease the
 * frequency of a VM application.
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>
 * Created on :Jan , 2019
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class RequestDispatcher extends AbstractComponent implements RequestNotificationHandlerI, ControllerManagementI,
		RequestSubmissionHandlerI, PushModeControllingI, RequestDispatcherManagementI {
	public static int DEBUG_LEVEL = 2;

	// -------------------------------------------------------------------------
	// Constants and instance variables
	// -------------------------------------------------------------------------

	protected String AppURI;
	/** the URI of the component. */
	protected final String rgURI;

	/** Counter Round-Robin */
	protected int counter = 0;

	/**
	 * Inbound port offering the request submission service of the
	 * RequestDispatcher.
	 */
	protected RequestSubmissionInboundPort requestSubmissionInboundPort;

	/** Outbound port used by the RequestDispatcher to notify tasks' termination. */
	protected RequestNotificationOutboundPort requestNotificationOutboundPort;

	/**
	 * RequestSubmissionOutboundPort ports used by the RequestDispatcher to Dispatch
	 * request to the VM
	 */
	protected ArrayList<RequestSubmissionOutboundPort> requestDispatcherSubmissionlist;

	/**
	 * RequestNotificationInboundPort ports used by the RequestDispatcher to Receive
	 * notification From the VM
	 */
	protected ArrayList<RequestNotificationInboundPort> requestDispatcherNotificationlist;

	protected HashMap<String, String> VMsubmission;

	/**
	 * URI of ports RequestSubmissionInboundPort used by the RequestDispatcher to
	 * Connect with the VM
	 */
	protected ArrayList<String> requestDispatcherInboundPortURI;

	/** URI of Computer RequestNotificationInboundPortURI */

	protected String requestNotificationInboundPortURI;

	/** HashMap to save Request Time Arrival time */

	protected HashMap<String, Long> StartRequest;

	/** HashMap to save Request notification Arrival time */

	protected HashMap<String, Long> EndRequest;

	/** HashMap to save Request number per ApplicationVM */

	protected HashMap<String, Integer> requestPerAVM;

	protected AverageDynamicStateDataInboundPort AverageDynamicStateDataInboundPort;

	/** Save last average */

	protected double average = 0.0;

	/** Current average */
	protected double currentavg;

	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;

	protected int compteur = 0;

	/** number of request notification */

	protected int n = 0;

	/** used to calculate average */

	protected double k = 0;

	/** RequestDispatcherManagementInboundPort */

	protected RequestDispatcherManagementInboundPort RequestDispatcherManagementInboundPort;

	/** ApplicationVM informations */

	protected HashMap<String, ApplicationVMPortsInformation> ApplicationVMs;

	/** PerformanceControllerManagementInboundPort */

	protected PerformanceControllerManagementInboundPort AdmissionControllerManagementInboundPort;

	/** Number of core */

	protected int nombrecore = 2;

	/** Released applicationVM */

	protected ArrayList<String> releasedVM;

	/**
	 * Create a RequestDispatcher component.
	 * 
	 * @param rgURI
	 *            RequestDispatcher URI
	 * @param requestSubmissionInboundPortURI
	 *            RequestSubmissionInboundPort URI
	 * @param requestDispatcherInboundPortURI
	 *            URI list of VM's RequestSubmissionInboundPort
	 * @param requestDispatchernotificationListt
	 *            URI List of RequestDispatcher RequestNotificationInboundPort
	 * @param requestDispatcherNotificationInboundPortURI
	 *            URI of RequestGenerator RequestNotificationInboundPort
	 * @param requestDispatcherNotificationOutboundPortURI
	 *            URI of RequestNotificationOutboundPort
	 * @throws Exception
	 */

	public RequestDispatcher(String rgURI, String AppURI, String requestSubmissionInboundPortURI,
			ArrayList<String> requestDispatcherInboundPortURI, ArrayList<String> requestDispatchernotificationListt,
			String requestDispatcherNotificationInboundPortURI, String requestDispatcherNotificationOutboundPortURI,
			String requestDispatcherManagementInboundPort,
			HashMap<String, ApplicationVMPortsInformation> ApplicationVMs) throws Exception {

		super(rgURI, 3, 3);

		// preconditions check

		assert requestSubmissionInboundPortURI != null;
		assert requestDispatchernotificationListt != null;
		assert requestDispatcherInboundPortURI != null;
		assert requestDispatcherNotificationInboundPortURI != null;
		assert requestDispatcherNotificationOutboundPortURI != null;

		// initialization
		this.AppURI = AppURI;
		this.rgURI = rgURI;
		this.requestNotificationInboundPortURI = requestDispatcherNotificationInboundPortURI;

		requestPerAVM = new HashMap<String, Integer>();

		requestDispatcherSubmissionlist = new ArrayList<RequestSubmissionOutboundPort>();
		this.addRequiredInterface(RequestSubmissionI.class);

		for (int i = 0; i < requestDispatcherInboundPortURI.size(); i++) {

			requestDispatcherSubmissionlist.add(new RequestSubmissionOutboundPort(this));
			this.addPort(requestDispatcherSubmissionlist.get(i));
			requestDispatcherSubmissionlist.get(i).publishPort();
		}

		this.addOfferedInterface(RequestNotificationI.class);

		this.requestDispatcherInboundPortURI = requestDispatcherInboundPortURI;

		requestDispatcherNotificationlist = new ArrayList<RequestNotificationInboundPort>();

		for (int i = 0; i < requestDispatchernotificationListt.size(); i++) {

			requestDispatcherNotificationlist
					.add(new RequestNotificationInboundPort(requestDispatchernotificationListt.get(i), this));
			this.addPort(requestDispatcherNotificationlist.get(i));
			requestDispatcherNotificationlist.get(i).publishPort();
		}

		this.requestSubmissionInboundPort = new RequestSubmissionInboundPort(requestSubmissionInboundPortURI, this);

		this.addPort(this.requestSubmissionInboundPort);
		this.requestSubmissionInboundPort.publishPort();

		this.addRequiredInterface(RequestNotificationI.class);

		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(
				requestDispatcherNotificationOutboundPortURI, this);
		this.addPort(this.requestNotificationOutboundPort);
		this.requestNotificationOutboundPort.publishPort();

		StartRequest = new HashMap<String, Long>();
		EndRequest = new HashMap<String, Long>();

		this.AverageDynamicStateDataInboundPort = new AverageDynamicStateDataInboundPort(AppURI + "-avgport", this);
		this.addPort(AverageDynamicStateDataInboundPort);
		this.AverageDynamicStateDataInboundPort.publishPort();

		this.addRequiredInterface(RequestDispatcherManagementI.class);
		this.RequestDispatcherManagementInboundPort = new RequestDispatcherManagementInboundPort(
				requestDispatcherManagementInboundPort, this);
		this.addPort(RequestDispatcherManagementInboundPort);
		this.RequestDispatcherManagementInboundPort.publishPort();
		this.VMsubmission = new HashMap<String, String>();

		this.ApplicationVMs = new HashMap<String, ApplicationVMPortsInformation>(ApplicationVMs);

		this.addOfferedInterface(ControllerManagementI.class);
		AdmissionControllerManagementInboundPort = new PerformanceControllerManagementInboundPort(AppURI + "-acminb",
				this);
		this.addPort(AdmissionControllerManagementInboundPort);
		AdmissionControllerManagementInboundPort.publishPort();

		this.releasedVM = new ArrayList<String>();
		
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
	@Override
	public void start() throws ComponentStartException {
		this.toggleTracing();
		this.toggleLogging();

		super.start();

		try {
			for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {
				this.doPortConnection(requestDispatcherSubmissionlist.get(i).getPortURI(),
						requestDispatcherInboundPortURI.get(i), RequestSubmissionConnector.class.getCanonicalName());
				VMsubmission.put(requestDispatcherSubmissionlist.get(i).getPortURI(), this.AppURI + "VM" + i);
				requestPerAVM.put(this.AppURI + "VM" + i, 0);
			}

		} catch (Exception e) {
			throw new ComponentStartException(e);
		}

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {
		for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {
			this.doPortDisconnection(requestDispatcherSubmissionlist.get(i).getPortURI());

		}

		super.finalise();

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */

	@Override
	public void shutdown() throws ComponentShutdownException {

		try {
			for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {

				requestDispatcherSubmissionlist.get(i).unpublishPort();
			}

			for (int i = 0; i < requestDispatcherNotificationlist.size(); i++) {

				requestDispatcherNotificationlist.get(i).unpublishPort();
			}

			this.requestNotificationOutboundPort.unpublishPort();
			this.requestSubmissionInboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}

	/**
	 * 
	 * This method allows to receive request notifications from applicationVM. upon
	 * receipt of each notification a new exponential moving average that allows
	 * focus on the new execution times without forgetting the old values ​​that
	 * lose their weight over time. this method allows to reduce the number of
	 * requests by ApplicationVM used to know the most usedVM application or the
	 * least. this method ensures the full release of the released applicationVM if
	 * it exists . At the end , it notify the RequestGenerator of request
	 * termination.
	 * 
	 * @param r
	 *            Request notification from ApplicationVM
	 * @param avm
	 *            ApplicationVM URI
	 */

	@Override
	public void acceptRequestTerminationNotification(RequestI r, String avm) throws Exception {
		assert r != null;

		// Update request number per ApplicationVM

		this.requestPerAVM.put(avm, this.requestPerAVM.get(avm) - 1);

		// Test if released ApplicationVM has finished all request in the queue

		if (!this.releasedVM.isEmpty() && !canReleaseVM().isEmpty()) {

			String releaseVMURI = canReleaseVM();
			this.logMessage("***** Ready to put ReleasedVM " + releaseVMURI + " in the Ring ******");

			canSendReleasedVM(releaseVMURI);
		}

		// Calculate Average

		long avg = System.nanoTime() - this.StartRequest.get(r.getRequestURI());
		this.n++;
		this.k = 2 / (double) (n + 1);
		this.currentavg = avg * this.k + this.currentavg * (1 - k);
		this.average = currentavg / (Double) 1000000000.0;

		this.logMessage("Request Dispatcher " + this.rgURI + " is notified that request " + r.getRequestURI()
				+ " has ended.then dispatch the notification to the Request Generator ");

		this.logMessage("********** le temps d'execution moyen *********" + currentavg);

		// Notify the RequestGenerator

		this.requestNotificationOutboundPort.notifyRequestTermination(r);
	}

	/**
	 * 
	 * 
	 * this method checks whether an application in the relasedVM list has completed
	 * all its request in order to release it and to send it to the
	 * AdmissionController
	 * 
	 * 
	 * @return URI of released ApplicationVM
	 */

	public String canReleaseVM() {

		String canReleaseVMURI = "";

		// Check if applicationVM from relasedVM has completed all its request

		for (int i = 0; i < this.releasedVM.size(); i++) {
			if (requestPerAVM.get(this.releasedVM.get(i)) == 0) {
				canReleaseVMURI = this.releasedVM.get(i);
				break;
			}
		}
		return canReleaseVMURI;
	}

	/**
	 * 
	 * this method receives requests from the RequestGenerator. it allows to keep
	 * the time of the arrival of a request in order to calculate the average
	 * afterwards. it performs the round robin algorithm to dispatch data to
	 * applicationVM
	 * 
	 * @param r
	 *            request from RequestGenerator
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(final RequestI r) throws Exception {

		this.logMessage(this.rgURI + "*** Accept Request *** " + r.getRequestURI());

		// Keep the arrival time of a Request URI

		StartRequest.put(r.getRequestURI(), System.nanoTime());

		// Send Request to the ApplicationVM in the List

		requestDispatcherSubmissionlist.get(counter).submitRequestAndNotify(r);

		String avmURI = requestDispatcherSubmissionlist.get(counter).getPortURI();

		// Adding the request numberPerAVL

		this.requestPerAVM.put(this.VMsubmission.get(avmURI),
				this.requestPerAVM.get(this.VMsubmission.get(avmURI)) + 1);

		// Round-Robin

		if (counter >= requestDispatcherSubmissionlist.size() - 1) {
			counter = 0;
		} else {
			counter++;
		}

	}

	/**
	 * getDynamicState return information about average and applicationVMs
	 * 
	 * @return Average , ApplicationURI , The most busy ApplicationVM and the Most
	 *         free ApplicationVM
	 * @throws Exception
	 */

	public AverageDynamicStateI getDynamicState() throws Exception {

		return new RequestDispatcherDynamicState(this.AppURI, this.average, getVMbusy(), getVMfree());
	}

	public void sendDynamicState() throws Exception {
		if (this.AverageDynamicStateDataInboundPort.connected()) {
			AverageDynamicStateI cds = this.getDynamicState();
			this.AverageDynamicStateDataInboundPort.send(cds);
			;
		}
	}

	public void sendDynamicState(final int interval, int numberOfRemainingPushes) throws Exception {
		this.sendDynamicState();
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1;
		if (fNumberOfRemainingPushes > 0) {
			this.pushingFuture = this.scheduleTask(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						((RequestDispatcher) this.getOwner()).sendDynamicState(interval, fNumberOfRemainingPushes);
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
					((RequestDispatcher) this.getOwner()).sendDynamicState();
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

		// first, send the static state if the corresponding port is connected

		this.pushingFuture = this.scheduleTask(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((RequestDispatcher) this.getOwner()).sendDynamicState(interval, n);
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
	 * 
	 * this method provides the connection of a new ApplicationVM to the Application
	 * it ensures the connection of the submission ports as well as the notification
	 * ports using the reflection port. at the end it updates the structures that
	 * keep informations about the ApplicationVM of the Application
	 * 
	 * @param RequestSubmissionInboundportURI
	 *            ApplicationVM RequestSubmissionInboundPortURI
	 * @param vMURI
	 *            ApplicationVM URI
	 * @param vMnotificationoutboundporturi
	 *            ApplicationVM NotificationOutboundPort
	 * @param appinfo
	 *            ApplicationVM Information
	 * 
	 */

	@Override
	public void ConnectVM(String RequestSubmissionInboundportURI, String vMURI, String vMnotificationoutboundporturi,
			ApplicationVMinformation appinfo) throws Exception {

		this.logMessage("***** Ready to Connect new ApplicationVM  ***** " + vMURI);

		ReflectionOutboundPort rop;
		rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();

		// Create new RequestNotificationInboundPort for the new ApplicationVM

		requestDispatcherNotificationlist.add(new RequestNotificationInboundPort(this));
		this.addPort(requestDispatcherNotificationlist.get(requestDispatcherNotificationlist.size() - 1));
		requestDispatcherNotificationlist.get(requestDispatcherNotificationlist.size() - 1).publishPort();

		// Use Reflection Port to Connect ApplicationVM RequestNotificationOutboundPort
		// with RequestDispatcher RequestNotificationInboundPort

		rop.doConnection(vMURI, ReflectionConnector.class.getCanonicalName());
		rop.doPortConnection(vMnotificationoutboundporturi,
				requestDispatcherNotificationlist.get(requestDispatcherNotificationlist.size() - 1).getPortURI(),
				RequestNotificationConnector.class.getCanonicalName());

		// Create a new RequestDispatcherSubmissionOutboundPort to Connect
		// RequestDispatcher with then new ApplicationVM

		requestDispatcherInboundPortURI.add(RequestSubmissionInboundportURI);
		requestDispatcherSubmissionlist.add(new RequestSubmissionOutboundPort(this));
		this.addPort(requestDispatcherSubmissionlist.get(requestDispatcherSubmissionlist.size() - 1));
		requestDispatcherSubmissionlist.get(requestDispatcherSubmissionlist.size() - 1).publishPort();

		// Connect the RequestDispatcherSubmissionOutboundPort with the ApplicationVM
		// RequestSubmissionInboundPort

		try {
			this.doPortConnection(
					requestDispatcherSubmissionlist.get(requestDispatcherSubmissionlist.size() - 1).getPortURI(),
					requestDispatcherInboundPortURI.get(requestDispatcherInboundPortURI.size() - 1),
					RequestSubmissionConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Save New ApplicationVM Informations

		this.requestPerAVM.put(vMURI, 0);
		this.VMsubmission.put(
				requestDispatcherSubmissionlist.get(requestDispatcherSubmissionlist.size() - 1).getPortURI(), vMURI);
		this.counter = requestDispatcherSubmissionlist.size() - 1;

		ApplicationVMinformation newVMinfo = appinfo;
		newVMinfo.setApplicationUri(this.AppURI);
		ApplicationVMPortsInformation applicationportinfo = new ApplicationVMPortsInformation(vMURI,
				RequestSubmissionInboundportURI, vMnotificationoutboundporturi, false, newVMinfo);
		this.ApplicationVMs.put(vMURI, applicationportinfo);

	}

	/**
	 * this method completely released an already inactive application. It removes
	 * the applicationVM from the VM list of the application and sends its
	 * information to the AdmissionController to add it to the ring network
	 * 
	 * @param releasedVM
	 *            URI of the releasedVM
	 * 
	 */

	public void canSendReleasedVM(String releasedVM) throws Exception {

		// Remove released applicationVM from RequestPerAVM (The released ApplicationVM
		// has finished all his request in the queue )

		this.requestPerAVM.remove(releasedVM);

		// Get information to send it to the AdmissionController

		ApplicationVMPortsInformation applicationVMinfo = new ApplicationVMPortsInformation(releasedVM,
				this.ApplicationVMs.get(releasedVM).getSubmissionInboundPortURI(),
				this.ApplicationVMs.get(releasedVM).getNotificationOutboundPortURI(), true,
				this.ApplicationVMs.get(releasedVM).getAppVMinfo());
		AdmissionControllerNotificationOutboundPort notificationOutboundPort = new AdmissionControllerNotificationOutboundPort(
				this);

		// Remove ApplicationVM from the list of Application AVM

		this.ApplicationVMs.remove(releasedVM);

		this.addPort(notificationOutboundPort);
		notificationOutboundPort.publishPort();

		// Do connection with the AdmissionController

		try {
			this.doPortConnection(notificationOutboundPort.getPortURI(), "-acnoinp",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Remove applicationVM from released List

		this.releasedVM.remove(releasedVM);

		// Send Notification

		notificationOutboundPort.updateRingVM(true, applicationVMinfo);

	}

	/**
	 * DisconnectVM disconnects an ApplicationVM from the application by
	 * disconnecting its submission port from the requestDispatcher port. the method
	 * keeps the connection with the notification port to ensure that the
	 * requestDispatcher has received all the notifications in order to fully
	 * release by sending the VM information to the AdmissionController to push it
	 * into the ring network
	 * 
	 * @param freeVM
	 *            The freest ApplicationVM
	 * 
	 */

	@Override
	public void DisconnectVM(String freeVM) throws Exception {

		boolean canDisconnectVM = false;

		// Check if the application has at least one applicationVM

		if (this.VMsubmission.size() > 1) {

			this.logMessage("**** Ready for Disconnection for ApplicationVM ***** " + freeVM);

			// Get the SubmissionOutboundPort Connected with ApplicationVM

			canDisconnectVM = true;
			String submissionURIport = "";
			for (Entry<String, String> submissionuri : this.VMsubmission.entrySet()) {
				if (submissionuri.getValue().equals(freeVM)) {
					submissionURIport = submissionuri.getKey();
				}
			}

			// Remove the SubmissionOutboundPort and do Disconnection with the ApplicationVM

			for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {
				if (submissionURIport.equals(requestDispatcherSubmissionlist.get(i).getPortURI())) {
					RequestSubmissionOutboundPort p = requestDispatcherSubmissionlist.get(i);
					this.requestDispatcherSubmissionlist.remove(i);
					this.VMsubmission.remove(p.getPortURI());
					try {
						this.doPortDisconnection(p.getPortURI());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						p.unpublishPort();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

			// RelasedVM list is used to check if the released ApplicationVM has completed
			// all request in queue

			this.counter = 0;
			this.releasedVM.add(freeVM);
		}

		// Send notification to PerformanceController

		AdmissionControllerNotificationOutboundPort requestdipatchertocontrollernotification = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(requestdipatchertocontrollernotification);
		requestdipatchertocontrollernotification.publishPort();

		try {
			this.doPortConnection(requestdipatchertocontrollernotification.getPortURI(), this.AppURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		requestdipatchertocontrollernotification.removeVMNotification(canDisconnectVM);

	}

	/**
	 * 
	 * this method is based on the number of pending requests for each
	 * applicationVM. it returns the URI of the applicationVM that has the most
	 * request in the queue
	 * 
	 * @throws Exception
	 * @return URI of the freest ApplicationVM
	 */

	public String getVMbusy() {

		String avmuri = "";
		int max = Integer.MIN_VALUE;

		// Check for the number of request per ApplicationVM

		for (Entry<String, Integer> requestavm : this.requestPerAVM.entrySet()) {

			// The URI of ApplicationVM most be different of released ApplicationVM (
			// released ApplicationVM should wait the termination of all request before
			// delete it from requestPerAVM

			if (requestavm.getValue() >= max && !this.releasedVM.contains(requestavm.getKey())) {
				max = requestavm.getValue();
				avmuri = requestavm.getKey();
			}

		}

		return avmuri;
	}

	/**
	 * 
	 * this method is based on the number of pending requests for each
	 * applicationVM. it returns the URI of the applicationVM that has the least
	 * request in the queue
	 * 
	 * @throws Exception
	 * @return URI of the freest ApplicationVM
	 */

	public String getVMfree() {

		String avmuri = "";
		int min = Integer.MAX_VALUE;

		// Check for the number of request per ApplicationVM

		for (Entry<String, Integer> requestavm : this.requestPerAVM.entrySet()) {

			// The URI of ApplicationVM most be different of released ApplicationVM (
			// released ApplicationVM should wait the termination of all request before
			// delete it from requestPerAVM

			if (requestavm.getValue() <= min && !this.releasedVM.contains(requestavm.getKey())) {
				min = requestavm.getValue();
				avmuri = requestavm.getKey();
			}
		}
		return avmuri;
	}

	/**
	 * addCores method tries to add a core number to the busiest ApplicationVM. The
	 * cores to be added must be on the same computer as the ApplicationVM. This
	 * searches if there are available resources and connect with the
	 * ApplicationVMManagement port to allocate resources to the ApplicationVM.
	 * 
	 * @throws Exception
	 */

	@Override
	public void addCores() throws Exception {

		boolean canAddCores = false;

		// Get the busiest ApplicationVM information from the list of ApplicationVMs

		ApplicationVMinformation appVMinfo = this.ApplicationVMs.get(getVMbusy()).getAppVMinfo();

		ComputerServicesOutboundPort csop;
		csop = new ComputerServicesOutboundPort(this);
		this.addPort(csop);
		csop.publishPort();

		// Connect to the same computer as the ApplicatioVM to search for resources

		String csipuri = appVMinfo.getComputerserviceuri();

		try {
			this.doPortConnection(csop.getPortURI(), csipuri, ComputerServicesConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		AllocatedCore[] alloctedcores;
		alloctedcores = csop.allocateCores(this.nombrecore);

		if (alloctedcores.length > 0) {

			// Allocate core to the ApplicationVM

			String avmmangamenturi = appVMinfo.getVMManagementuri();
			this.addOfferedInterface(ApplicationVMManagementI.class);
			ApplicationVMManagementOutboundPort avmop = new ApplicationVMManagementOutboundPort(this);
			this.addPort(avmop);
			avmop.publishPort();

			try {

				this.doPortConnection(avmop.getPortURI(), avmmangamenturi,
						ApplicationVMManagementConnector.class.getCanonicalName());

			} catch (Exception e) {
				e.printStackTrace();
			}

			canAddCores = true;
			avmop.allocateCores(alloctedcores);

		}
		// Send Notification to the PerformanceController

		AdmissionControllerNotificationOutboundPort requestdipatchertocontrollernotification = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(requestdipatchertocontrollernotification);
		requestdipatchertocontrollernotification.publishPort();

		try {
			this.doPortConnection(requestdipatchertocontrollernotification.getPortURI(), this.AppURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		requestdipatchertocontrollernotification.addcoresNotification(canAddCores);
	}

	/**
	 * removeCores method tries to remove a core number from the freest
	 * ApplicationVM. If the number of core of an ApplicationVM exceeds 1, the
	 * method connects to the ApplicationVM Management to deallocate a number of
	 * cores. at the end it sends a notification to the PerformanceController
	 *
	 * @throws Exception
	 */

	@Override
	public void removeCores() throws Exception {

		boolean canRemoveCores = false;

		// Get the freest ApplicationVM information from the list of ApplicationVMs

		ApplicationVMinformation appVMinfo = this.ApplicationVMs.get(getVMfree()).getAppVMinfo();

		// Check if the ApplicationVM have a number of cores greater than 1

		if (appVMinfo.getAllocatedcore().length > 1) {

			String avmmangamenturi = appVMinfo.getVMManagementuri();
			this.addOfferedInterface(ApplicationVMManagementI.class);
			ApplicationVMManagementOutboundPort avmop = new ApplicationVMManagementOutboundPort(this);
			this.addPort(avmop);
			avmop.publishPort();

			// Connect to ApplicationVMManagementPort to deallocate cores from ApplicationVM

			try {

				this.doPortConnection(avmop.getPortURI(), avmmangamenturi,
						ApplicationVMManagementConnector.class.getCanonicalName());

			} catch (Exception e) {
				e.printStackTrace();
			}
			canRemoveCores = true;
			avmop.deallocateCores(appVMinfo.getComputerserviceuri(), 1);

		}

		// Send notification to the PerformanceController

		AdmissionControllerNotificationOutboundPort requestdipatchertocontrollernotification = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(requestdipatchertocontrollernotification);
		requestdipatchertocontrollernotification.publishPort();

		try {
			this.doPortConnection(requestdipatchertocontrollernotification.getPortURI(), this.AppURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		requestdipatchertocontrollernotification.removecoresNotification(canRemoveCores);

	}

	/**
	 * The IncreaseFrequencyControl method increases the frequency of all
	 * ApplicationVMs in the application. the method retrieves the service ports of
	 * the computers to request the increase of the frequency. a notification is
	 * sent to the PerformanceController to inform it of the status of the frequency
	 * change
	 *
	 * @throws Exception
	 */

	@Override
	public void IncreaseFrequencyControl() throws Exception {

		boolean state = false;
		boolean stateallVM = true;

		// Get computer URI for each Application VM to get the ComputerPortServiceURI

		for (Entry<String, ApplicationVMPortsInformation> entry : this.ApplicationVMs.entrySet()) {
			String csipuri = entry.getValue().getAppVMinfo().getComputerserviceuri();

			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();

			// Connection to the ComputerServiceInboundPort

			try {
				this.doPortConnection(csop.getPortURI(), csipuri, ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Increase Frequency of VM allocatedCores

			try {
				state = csop.IncreaseFrequency(entry.getValue().getAppVMinfo().getAllocatedcore());
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!state == true) {
				stateallVM = false;
			}
		}

		// Notify the PerformanceController

		AdmissionControllerNotificationOutboundPort requestdipatchertocontrollernotification = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(requestdipatchertocontrollernotification);
		requestdipatchertocontrollernotification.publishPort();

		try {
			this.doPortConnection(requestdipatchertocontrollernotification.getPortURI(), this.AppURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		requestdipatchertocontrollernotification.frequencyIncreaseNotification(stateallVM);

	}

	/**
	 * The DecreaseFrequencyControl method decreases the frequency of all
	 * ApplicationVMs in the application. the method retrieves the service ports of
	 * the computers to request the decrease of the frequency. a notification is
	 * sent to the PerformanceController to inform it of the status of the frequency
	 * change
	 *
	 * @throws Exception
	 */

	@Override
	public void DecreaseFrequencyControl() throws Exception {

		boolean decreasefrequency = false;
		boolean stateallVM = true;

		// Get computer URI for each Application VM to get the ComputerPortServiceURI

		for (Entry<String, ApplicationVMPortsInformation> entry : this.ApplicationVMs.entrySet()) {
			String csipuri = entry.getValue().getAppVMinfo().getComputerserviceuri();

			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();

			// Connection to the ComputerServiceInboundPort

			try {
				this.doPortConnection(csop.getPortURI(), csipuri, ComputerServicesConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Decrease Frequency of VM allocatedCores

			try {
				decreasefrequency = csop.DecreaseFrequency(entry.getValue().getAppVMinfo().getAllocatedcore());

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!decreasefrequency == true) {
				stateallVM = false;
			}
		}

		// Notify the PerformanceController

		AdmissionControllerNotificationOutboundPort requestdipatchertocontrollernotification = new AdmissionControllerNotificationOutboundPort(
				this);
		this.addPort(requestdipatchertocontrollernotification);
		requestdipatchertocontrollernotification.publishPort();

		try {
			this.doPortConnection(requestdipatchertocontrollernotification.getPortURI(), this.AppURI + "-cacnotif",
					AdmissionControllerNotificationConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		requestdipatchertocontrollernotification.frequencyDecreaseNotification(decreasefrequency);

	}

	@Override
	public void CreateVM(String ApplicationURI, String requestDispatchermanagementuri, int numberVM) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void DestroyVM(String applicationURI, String appVM) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

	}

}