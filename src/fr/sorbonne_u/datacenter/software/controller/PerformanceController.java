package fr.sorbonne_u.datacenter.software.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.ui.RefineryUtilities;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.connectors.ControlledDataConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerNotificationI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;
import fr.sorbonne_u.datacenter.software.controller.connectors.PerformanceContollerManagementConnector;
import fr.sorbonne_u.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.sorbonne_u.datacenter.software.controller.interfaces.RingnetworkI;
import fr.sorbonne_u.datacenter.software.controller.ports.PerformanceControllerManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.controller.ports.RingnetworkCoordinatorInboundPort;
import fr.sorbonne_u.datacenter.software.controller.ports.RingnetworkCoordinatorOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDataConsumerI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDynamicStateI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.AverageDynamicStateDataOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;
import fr.sorbonne_u.datacenterclient.utils.AveragePerformanceChart;

/**
 * The class <code>PerformanceController</code> the class performance controller
 * to control the operation of an application by applying several strategies to
 * maintain a normal execution time
 * 
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * this component regularly receives data on the average execution of requests
 * from an application and decides to apply a strategy to maintain a suitable
 * execution rate. in order to have a good execution time a higher bound and a
 * lower bound allow to limit the interval of execution. Beyond the upper limit,
 * the Performance Controller decides to increase the frequency. in case of
 * failure, it tries to add a core to an applicationVM. in a new failure case,
 * it tries to get a VM application from RingNetwork and asks the
 * RequestDispatcher to connect it to the application. In extreme cases, it asks
 * the admissions controller to create an applicationVM for the application
 * without going through the RingNetwork.
 * 
 * below the lower bound, the application can free resources. The
 * PerformanceController can decrease the frequency of the cores or remove cores
 * from an applicationVM or order the RequestDispatcher to disconnect an
 * ApplicationVM from the application
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
 * Created on : Jan,2019
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class PerformanceController extends AbstractComponent
		implements AverageDataConsumerI, AdmissionControllerNotificationI, RingnetworkI {

	public enum strategy {
		FREQUENCY_CONTROL, CORE_CONTROL, VM_CONTROL;
	}

	public enum decision {
		NONE, IS_INCREASE, IS_DECREASE;
	}

	protected HashMap<strategy, decision> controlStatus;

	/** boolean indicate if the application need VM from the ringNetwork */

	protected boolean needVM = false;

	/** Time needed to start control */

	protected double control = 15.0;

	/** lock used to stop control if another control is launched */

	protected boolean lock = true;

	/** The PerformanceController URI */

	protected String controllerURI;

	/** The Upperbound used to start control */

	protected double upperbound ;

	/** The Lowerbound used to start control */

	protected double lowerbound ;

	/** The starttime indicate the PerformanceController startime */

	protected double startime;

	/**
	 * The endtime indicate the PerformanceController current time when
	 * acceptingData from RequestDispatcher
	 */

	protected double endtime;

	/** The RequestDispatcher DynamicDataInbounPort URI */

	protected String RequestDispatcherDynamicDataInboundPortURI;

	/** DynamicStateDataOutboundPort used to receive Average */

	protected AverageDynamicStateDataOutboundPort avgPort;

	protected HashMap<String, Double> ApplicationAVG;

	/** Average of the application */

	protected double appAVG = 0.0;

	/** Port used to execute control in RequestDispatcher and admissionController */

	protected PerformanceControllerManagementOutboundPort admissionControllerManagementOutboundPort;

	/** The most busy ApplicationVM */

	protected String busyvm;

	/** The most free ApplicationVM */
	protected String freevm;

	/** Application URI */

	protected String ApplicationURI;

	/** RequestDispatcher ManagementInboundPort URI */

	protected String RequestDispatcherManagementURI;

	/** The number of VM per Application */

	protected int numberVM;

	/** Port RingnetworkCoordinatorInboundPort used to connect in the RingNetwork */
	protected RingnetworkCoordinatorInboundPort ringnetwordinboundport;

	/**
	 * Port RingnetworkCoordinatorOutboundPort used to connect in the RingNetwork
	 */
	protected RingnetworkCoordinatorOutboundPort ringnetworkoutboundport;

	/**
	 * Port AdmissionControllerNotificationInboundPort used to receive notification
	 * from RequestDispatcher and AdmissionController
	 */

	protected AdmissionControllerNotificationInboundPort admissioncontrollernotificationin;

	/** the chart used to display average */

	protected AveragePerformanceChart chart;

	/**
	 * The Constructor of the component PerformanceController
	 * 
	 * 
	 * @param ApplicationURI
	 *            Application URI
	 * @param controllerURI
	 *            PerformanceController URI
	 * @param RequestDispatcher
	 *            RequestDispatcher URI
	 * @param RequestDispatcherDynamicDataInboundPortURI
	 *            Port for Accepting Dynamic Data
	 * @param requestDispatcherManagementInboundURI
	 *            RequestDispatcherManagementInbound URI to invoke control methods
	 * @param ringinboundPortURI
	 *            Port RingnetworkCoordinatorInboundPort URI used for the
	 *            RingNetwork
	 * @param ringOutboundPortURI
	 *            Port RingnetworkCoordinatorOutboundPort URI used for the
	 *            RingNetwork
	 * @throws Exception
	 */

	public PerformanceController(String ApplicationURI, String controllerURI, String RequestDispatcher,
			String RequestDispatcherDynamicDataInboundPortURI, String requestDispatcherManagementInboundURI,
			String ringinboundPortURI, String ringOutboundPortURI, Integer numberVM,Double lowerbound , Double upperbound , 
			HashMap<strategy, decision> controlStatus) throws Exception {

		super(controllerURI, 2, 2);

		this.numberVM = numberVM;
		this.RequestDispatcherManagementURI = requestDispatcherManagementInboundURI;
		this.controllerURI = controllerURI;
		this.ApplicationURI = ApplicationURI;
		this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.ApplicationAVG = new HashMap<String, Double>();
		this.RequestDispatcherDynamicDataInboundPortURI = RequestDispatcherDynamicDataInboundPortURI;

		this.avgPort = new AverageDynamicStateDataOutboundPort(this, RequestDispatcher);
		this.addPort(avgPort);
		this.avgPort.publishPort();
		this.tracer.setTitle(this.controllerURI);
		this.tracer.setRelativePosition(3, 0);

		this.addRequiredInterface(ControllerManagementI.class);
		this.admissionControllerManagementOutboundPort = new PerformanceControllerManagementOutboundPort(this);
		this.addPort(admissionControllerManagementOutboundPort);
		this.admissionControllerManagementOutboundPort.publishPort();

		this.admissioncontrollernotificationin = new AdmissionControllerNotificationInboundPort(
				this.ApplicationURI + "-cacnotif", this);
		this.addPort(admissioncontrollernotificationin);
		this.admissioncontrollernotificationin.publishPort();

		this.ringnetwordinboundport = new RingnetworkCoordinatorInboundPort(ringinboundPortURI, this);
		this.addPort(ringnetwordinboundport);
		this.ringnetwordinboundport.publishPort();

		this.ringnetworkoutboundport = new RingnetworkCoordinatorOutboundPort(ringOutboundPortURI, this);
		this.addPort(ringnetworkoutboundport);
		this.ringnetworkoutboundport.publishPort();

		this.controlStatus = new HashMap<strategy, decision>(controlStatus);
		this.lowerbound = lowerbound ; 
		this.upperbound = upperbound ; 
		
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

		this.toggleTracing();
		this.toggleLogging();
		super.start();

		// Create a new chart

		chart = new AveragePerformanceChart(this.ApplicationURI + "Average Chart");
		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);

		// start the pushing of dynamic state information from RequestDispatcher

		try {
			this.doPortConnection(this.avgPort.getPortURI(), this.RequestDispatcherDynamicDataInboundPortURI,
					ControlledDataConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(
					"Unable to start the pushing of dynamic data from" + " the comoter component.", e);
		}

		try {
			this.doPortConnection(this.admissionControllerManagementOutboundPort.getPortURI(),
					this.ApplicationURI + "-acminb", PerformanceContollerManagementConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(
					"Unable to start the pushing of dynamic data from" + " the comoter component.", e);
		}

		try {
			this.avgPort.startUnlimitedPushing(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.startime = System.nanoTime();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */

	public void finalise() throws Exception {
		try {
			if (this.avgPort.connected()) {
				this.avgPort.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("port disconnection error", e);
		}
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {
			this.avgPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException("port unpublishing error", e);
		}

		super.shutdown();
	}

	/**
	 *
	 * this method regularly accepts Data from requestDispatcher. the data indicate
	 * information on the average, the freest ApplicationVM and the busiest
	 * ApplicationVM. this method controls the beginning of the control
	 * 
	 * @param Component
	 * 
	 * @param currentDynamicState
	 *            Containing Data from RequestDispatcher
	 */

	@Override
	public void acceptAverageData(String Component, AverageDynamicStateI currentDynamicState) throws Exception {

		// Current time system to check the possibility of control

		this.endtime = System.nanoTime();

		boolean startcontrol = false;

		this.logMessage("**** Average : " + currentDynamicState.getAveragePerApplication() + "*****");
		this.logMessage("*** Busy ApplicationVM : " + currentDynamicState.getvmbusy() + "*****");
		this.logMessage("*** Free ApplicationVM : " + currentDynamicState.getVMfree() + "*****");

		// Trace the average chart

		chart.traceAverage(currentDynamicState.getAveragePerApplication());

		// Update information about average , busiest ApplicationVM and freest
		// ApplicationVM

		this.ApplicationURI = currentDynamicState.getApplicationURI();
		this.busyvm = currentDynamicState.getvmbusy();
		this.freevm = currentDynamicState.getVMfree();

		// Check if the new average is different from the last average ( Verify if the
		// average have changed after regulation time )

		if (currentDynamicState.getAveragePerApplication() != this.appAVG) {
			this.appAVG = currentDynamicState.getAveragePerApplication();

			startcontrol = true;
		}

		this.ApplicationAVG.put(currentDynamicState.getApplicationURI(),
				currentDynamicState.getAveragePerApplication());

		// Current time

		double result = (this.endtime - this.startime) / (Double) 1_000_000_000.0;

		// Current time is upper then the control time (Regulation) , the average is
		// upper then 0 and the average have changed after regulation time

		if (result > this.control && currentDynamicState.getAveragePerApplication() > 0 && startcontrol) {

			// if the lock is free then start control

			if (this.lock == true) {
				this.lock = false;
				startcontrol();
			}
		}
	}

	/**
	 *
	 * this method is used to indicate the sens of the control and start with
	 * frequencyControl
	 * 
	 */

	public void startcontrol() throws Exception {

		this.logMessage("**** Start Control ***** ");
		if (this.appAVG > this.upperbound) {

			// If the Average is upper then upper bound then try to increase frequency

			this.logMessage("**** Average is upper then the upperbound ***** ");
			this.logMessage("**** Trying to increase Frequency **** ");
			
			if(!this.controlStatus.get(strategy.FREQUENCY_CONTROL).equals(decision.IS_INCREASE)) {
				increaseFrequency();
			}
			else if(!this.controlStatus.get(strategy.CORE_CONTROL).equals(decision.IS_INCREASE)) {
				addCoresRequest();
			}
			else {
				this.needVM = true;
			}
		
			

		} else if (this.appAVG < this.lowerbound) {

			// If the Average is lower then lower bound then try to decrease frequency

			this.logMessage("**** Average is lower then the lowerbound ***** ");
			this.logMessage("**** Trying to decrease Frequency **** ");

			if(!this.controlStatus.get(strategy.FREQUENCY_CONTROL).equals(decision.IS_DECREASE)) {
				decreaseFrequency();
			}
			else if(!this.controlStatus.get(strategy.CORE_CONTROL).equals(decision.IS_DECREASE)) {
				removeCoresRequest();
			}
			else {
				disconnectVM();
			}

		}

		else {

			// if the Average is normal then release lock

			this.lock = true;
		}
	}

	/**
	 * 
	 * this method is used to send an RequestDispatcher request to Increase Core
	 * 
	 */
	public void increaseFrequency() throws Exception {

		this.controlStatus.replace(strategy.FREQUENCY_CONTROL,decision.IS_INCREASE);
		admissionControllerManagementOutboundPort.IncreaseFrequencyControl();
	}

	/**
	 * 
	 * this method is used to send an RequestDispatcher request to decrease
	 * frequency
	 * 
	 */
	public void decreaseFrequency() throws Exception {

		this.controlStatus.replace(strategy.FREQUENCY_CONTROL,decision.IS_DECREASE);
		admissionControllerManagementOutboundPort.DecreaseFrequencyControl();

	}

	/**
	 *
	 * the frequencyIncreaseNotification method receives a notification from the
	 * RequestDispatcher to indicate the result of increasing frequency . if state
	 * is false , the PerformanceController try to add cores
	 * 
	 *
	 * if state is true, it releases the lock and applies a regulation time before
	 * the next control
	 *
	 * @param state
	 *            the result of increasing frequency
	 * 
	 */

	@Override
	public void frequencyIncreaseNotification(Boolean state) throws Exception {

		if (state == false) {

			// If RequestDispatcher can not decrease frequency , PerformanceController try
			// to add cores from busiest ApplicationVM

			this.logMessage("**** Can not increase Frequency ***** ");
			this.logMessage("**** Trying to add cores to " + this.busyvm + " **** ");

			addCoresRequest();

		} else {

			// Release lock

			this.lock = true;

			// Regulation Time (The next Control will be in 15 s )
			double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
			this.control = this.control + regulation;
			this.logMessage("**** Increasing Frequency Successfuly ***** ");
			this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
		}
	}

	/**
	 *
	 * the frequencyDecreaseNotification method receives a notification from the
	 * RequestDispatcher to indicate the result of decreasing frequency . if state
	 * is false , the PerformanceController try to remove cores .
	 *
	 * if state is true, it releases the lock and applies a regulation time before
	 * the next control
	 *
	 * @param state
	 *            the result of decreasing frequency
	 * 
	 */

	@Override
	public void frequencyDecreaseNotification(Boolean state) throws Exception {

		if (state == false) {

			// If RequestDispatcher can not decrease frequency , PerformanceController try
			// to remove cores from freest ApplicationVM

			this.logMessage("**** Can not decrease Frequency ***** ");
			this.logMessage("**** Trying to remove cores to " + this.freevm + " **** ");

			removeCoresRequest();

		} else {

			// Release lock

			this.lock = true;

			// Regulation Time (The next Control will be in 15 s )

			double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
			this.control = this.control + regulation;

			this.logMessage("**** Decreasing Frequency Successfuly ***** ");
			this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
		}
	}

	/**
	 * 
	 * this method is used to send an RequestDispatcher request to add Core
	 * 
	 */

	public void addCoresRequest() throws Exception {

		this.controlStatus.replace(strategy.CORE_CONTROL,decision.IS_INCREASE);
		admissionControllerManagementOutboundPort.addCores();

	}

	/**
	 * 
	 * this method is used to send an RequestDispatcher request to remove Core
	 * 
	 */

	public void removeCoresRequest() throws Exception {

		this.controlStatus.replace(strategy.CORE_CONTROL,decision.IS_DECREASE);
		admissionControllerManagementOutboundPort.removeCores();

	}

	/**
	 *
	 * the addcoresNotification method receives a notification from the
	 * RequestDispatcher to indicate the result of adding cores . if state is false
	 * , the PerformanceController try to add ApplicationVM
	 * 
	 *
	 * if state is true, it releases the lock and applies a regulation time before
	 * the next control
	 *
	 * @param state
	 *            the result of adding cores
	 * 
	 */

	@Override
	public void addcoresNotification(boolean findressouce) throws Exception {

		if (findressouce == false) {

			// If RequestDispatcher can not remove cores , PerformanceController try to pick
			// up ApplicationVM from RingNetwork

			this.logMessage("**** Can not add cores ***** ");
			this.logMessage("**** Trying to add new VM  **** ");

			this.needVM = true;

		} else {

			// Release lock

			this.lock = true;

			// Regulation Time (The next Control will be in 15 s )

			double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
			this.control = this.control + regulation;

			this.logMessage("**** Adding cores Successfuly ***** ");
			this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
		}
	}

	/**
	 *
	 * the removecoresNotification method receives a notification from the
	 * RequestDispatcher to indicate the result of removing cores . if state is
	 * false , the PerformanceController try to disconnect ApplicationVM
	 * 
	 *
	 * if state is true, it releases the lock and applies a regulation time before
	 * the next control
	 *
	 * @param state
	 *            the result of removing cores
	 * 
	 */

	@Override
	public void removecoresNotification(boolean state) throws Exception {

		if (state == false) {

			// If RequestDispatcher can not remove cores , PerformanceController try to
			// disconnect ApplicationVM

			this.logMessage("**** Can not Remove cores ***** ");
			this.logMessage("**** Trying to Remove VM  **** ");

			disconnectVM();

		} else {

			// Release lock

			this.lock = true;

			// Regulation Time (The next Control will be in 15 s )

			double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
			this.control = this.control + regulation;

			this.logMessage("**** Removing cores Successfuly ***** ");
			this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
		}

	}

	/**
	 * 
	 * this method is used to send an AdmissionController request to Create new
	 * ApplicationVM and connect it to the Application
	 * 
	 */

	public void addVM() throws Exception {

		// Connect to AdmissionController to create new ApplicationVM the freest
		// ApplicationVM

		PerformanceControllerManagementOutboundPort portToAdmissionController;
		portToAdmissionController = new PerformanceControllerManagementOutboundPort(this);
		this.addPort(portToAdmissionController);
		portToAdmissionController.publishPort();

		try {
			this.doPortConnection(portToAdmissionController.getPortURI(), "-admssionManagement",
					PerformanceContollerManagementConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		portToAdmissionController.CreateVM(this.ApplicationURI, this.RequestDispatcherManagementURI, numberVM);
	}

	/**
	 * 
	 * this method is used to send a RequestDispatcher request to disconnect the
	 * Most free ApplicationVM In the Application
	 * 
	 * 
	 */

	public void disconnectVM() throws Exception {

		// Connect to RequestDispatcher to Disconnect the freest ApplicationVM

		RequestDispatcherManagementOutboundPort rmop = new RequestDispatcherManagementOutboundPort(this);
		this.addPort(rmop);
		rmop.publishPort();

		try {
			this.doPortConnection(rmop.getPortURI(), RequestDispatcherManagementURI,
					RequestDispatcherManagementConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		rmop.DisconnectVM(this.freevm);
		
		
	}

	/**
	 * The receiveVM method receive ApplicationVM informations from the RingNetwok
	 * and verify if the PerformanceController need an ApplicationVM . In that case
	 * , it checks if the Ring ApplicationVM are free. he keeps informations if it
	 * find a free ApplicationVM and transmits it to the RequestDispatcher that will
	 * connect to the ApplicationVM. at the end the method updates the information
	 * in the RingNetwork
	 * 
	 * @param ringappVM
	 *            Contain VM's informations circulating in the RingNetwork
	 * 
	 */

	@Override
	public void receiveVM(ArrayList<ApplicationVMPortsInformation> ringappVM) throws Exception {

		boolean findVM = false;

		Thread.sleep(100);

		ArrayList<ApplicationVMPortsInformation> appVM = new ArrayList<ApplicationVMPortsInformation>(ringappVM);

		ApplicationVMinformation vmInfo = new ApplicationVMinformation();

		String vmURI = "";
		String submssioninboundport = "";
		String notificationoutboundport = "";

		// if the PerformanceController need Application VM and the size of the copy of
		// Ring ApplicationVM is not empty

		if (this.needVM == true) {
			if (appVM.size() > 0) {

				// Look for unused ApplicationVM

				for (int i = 0; i < appVM.size(); i++) {
					if (appVM.get(i).isFreeVM()) {

						// Pick up ApplicationVM information from RingNetwork

						vmURI = appVM.get(i).getApplicationVMURI();

						this.logMessage("*** Free applicatonVM " + vmURI + " from Ring Network ***");
						submssioninboundport = appVM.get(i).getSubmissionInboundPortURI();
						notificationoutboundport = appVM.get(i).getNotificationOutboundPortURI();
						vmInfo = appVM.get(i).getAppVMinfo();

						// Change the status of the applicationVM without remove information

						appVM.get(i).setFreeVM(false);

						findVM = true;
						break;

					}
				}

				if (findVM) {

					// Sending ApplicationVM information to RequestDispatcher to connect the new VM

					RequestDispatcherManagementOutboundPort rmop = new RequestDispatcherManagementOutboundPort(this);
					this.addPort(rmop);
					rmop.publishPort();

					try {
						this.doPortConnection(rmop.getPortURI(), RequestDispatcherManagementURI,
								RequestDispatcherManagementConnector.class.getCanonicalName());
					} catch (Exception e) {
						System.out.println(e);
					}

					this.needVM = false;
					this.logMessage("*** Ready to Add new ApplicationVM From Ring Network to Application ***");
					rmop.ConnectVM(submssioninboundport, vmURI, notificationoutboundport, vmInfo);

					// Release the lock

					this.lock = true;

					// Regulation Time (The next Control will be in 15 s )

					double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
					this.control = this.control + regulation;

				} else {

					// If the list of ApplicationVM in the RingNetwork is empty

					this.needVM = false;

					// Try to add new ApplicationVM

					addVM();
				}
			} else {

				// If all ApplicationVM in the RingNetwork are used

				this.needVM = false;

				// Try to add new ApplicationVM

				addVM();
			}
		}

		// Send ApplicationVM list to the next peer

		this.ringnetworkoutboundport.receiveVM(appVM);
	}

	/**
	 *
	 * the addVMNotification method receives a notification from the
	 * AdmissionControlleur to indicate the result of adding an applicationVM if
	 * state is true, it releases the lock and applies a regulation time before the
	 * next control
	 * 
	 * @param state
	 *            the result of adding an applicationVM
	 * 
	 */

	@Override
	public void addVMNotification(Boolean state) throws Exception {

		// Release the lock
		
		this.controlStatus.put(strategy.FREQUENCY_CONTROL, decision.NONE);
		this.controlStatus.put(strategy.CORE_CONTROL, decision.NONE);
		this.controlStatus.put(strategy.VM_CONTROL, decision.NONE);

		this.lock = true;

		// Regulation Time (The next Control will be in 15 s )

		double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
		this.control = this.control + regulation;

		// Status of the control

		if (state == true) {

			this.logMessage("**** Adding VM Successfuly ***** ");
		} else {

			this.logMessage("**** Can not add VM ***** ");
		}

		this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
	}

	/**
	 *
	 * the removeVMNotification method receives a notification from the
	 * RequestDispatcher to indicate the result of Disconnecting an applicationVM.
	 * it releases the lock and applies a regulation time before the next control
	 * 
	 * @param state
	 *            the result of Disconnecting an applicationVM
	 * 
	 */

	@Override
	public void removeVMNotification(Boolean state) throws Exception {

		this.controlStatus.put(strategy.FREQUENCY_CONTROL, decision.NONE);
		this.controlStatus.put(strategy.CORE_CONTROL, decision.NONE);
		this.controlStatus.put(strategy.VM_CONTROL, decision.NONE);
		
		
		// Release the lock

		this.lock = true;

		// Regulation Time (The next Control will be in 15 s )

		double regulation = (System.nanoTime() - this.startime) / (Double) 1_000_000_000.0;
		this.control = this.control + regulation;

		// Status of the control

		if (state == true) {

			this.logMessage("**** Remove VM Successfuly ***** ");
		} else {

			this.logMessage("**** Can not Remove VM ***** ");
		}

		this.logMessage("***** Waiting for the regulation Time " + regulation + " *****");
	}

	@Override
	public void updateRingVM(boolean exist, ApplicationVMPortsInformation appinfo) throws Exception {
		// TODO Auto-generated method stub

	}

}
