package fr.sorbonne_u.datacenterclient.tests;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.tests.ComputerMonitor;
import fr.sorbonne_u.datacenter.software.Monitor.ApplicationMonitor;
import fr.sorbonne_u.datacenter.software.admissioncontroller.AdmissionController;
import fr.sorbonne_u.datacenter.software.application.Application;
import fr.sorbonne_u.datacenter.software.controller.PerformanceController.decision;
import fr.sorbonne_u.datacenter.software.controller.PerformanceController.strategy;

/**
 * The class <code>TestProject_3</code> This test case allows you to see all the
 * features implemented during the project
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * this first test case, is the case of general test of our project. we create 2
 * computers each contains 4 processors and 2 cores per processor. we chose this
 * number of processors because we have 2 applications that each require 4 cores
 * and 2 ApplicationVM in the Ring Network that require 4 cores. the rest of the
 * cores will be used to add new ApplicationVM or add cores. we set the upper
 * bound to 20 and the lower bound 10 (Experimentally, the ideal values ​​of
 * executions are between its two bounds). The control strategy in this test
 * case is our basic strategy (control on frequency - cores control,
 * ApplicationVM control)
 * </p>
 * <p>
 * we set the lower bound to 20 and the upper bound 10 to get more run time
 * below the lower bound for more resource release execution (Remove cores
 * Disconnect applicationVM - Decrease frequency ). we start the control with
 * cores control to increase probability to see ApplicationVM Release execution
 * 
 * </p>
 * <p>
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
 * Created on : Jan, 2018
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public class TestProject_3 extends AbstractCVM {
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String ComputerServicesInboundPortURI = "cs-ibp";
	public static final String ComputerStaticStateDataInboundPortURI = "css-dip";
	public static final String ComputerDynamicStateDataInboundPortURI = "cds-dip";

	public static final String ComputerServicesInboundPortURI1 = "cs-ibp1";
	public static final String ComputerStaticStateDataInboundPortURI1 = "css-dip1";
	public static final String ComputerDynamicStateDataInboundPortURI1 = "cds-dip1";

	public static final String ApplicationVMManagementInboundPortURI = "avm-ibp";
	public static final String ApplicationVMManagementInboundPortURI1 = "avm1-ibp";
	public static final String ApplicationVMManagementInboundPortURI2 = "avm2-ibp";

	public static final String RequestGeneratorManagementInboundPortURI = "rgmip";
	public static final String RequestGeneratorManagementInboundPortURI1 = "rgmip1";
	public static final String RequestGeneratorManagementInboundPortURI2 = "rgmip2";
	// --------------------------------------------------------------------------------

	/** Computer monitor component. */
	protected ApplicationMonitor cm;
	protected ComputerMonitor computermonitor1, computermonitor2;
	/** Applications */
	protected Application app1, app2, app3;

	/** cores per Computer */
	protected Hashtable<String, Integer> cores;
	/** Computer URI and ServiceInboundPortURI */

	protected Hashtable<String, String> Computeruriservice;

	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public TestProject_3() throws Exception {
		super();
	}

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	@Override
	public void deploy() throws Exception {
		Processor.DEBUG = true;

		// Computer with 4 Processors , 2 Cores

		String computerURI = "computer0";
		int numberOfProcessors = 4;
		int numberOfCores = 2;

		// Frequencies

		Set<Integer> admissibleFrequencies = new HashSet<Integer>();
		admissibleFrequencies.add(1000);
		admissibleFrequencies.add(1500);
		admissibleFrequencies.add(3000);
		admissibleFrequencies.add(4000);

		Map<Integer, Integer> processingPower = new HashMap<Integer, Integer>();

		// ProcessingPower
		processingPower.put(1000, 1000000);
		processingPower.put(1500, 1500000);
		processingPower.put(3000, 3000000);
		processingPower.put(4000, 4000000);

		// the real
		// MaxfrequencyGap
		// is fixed in
		// Coordinator 1000;

		Computer c = new Computer(computerURI, admissibleFrequencies, processingPower, 3000, 4000, numberOfProcessors,
				numberOfCores, ComputerServicesInboundPortURI, ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(c);

		// Computer with 4 Processors , 2 Cores

		String computerURI1 = "computer1";
		int numberOfProcessors1 = 4;
		int numberOfCores1 = 2;

		// Frequencies

		Set<Integer> admissibleFrequencies1 = new HashSet<Integer>();
		admissibleFrequencies1.add(1000);
		admissibleFrequencies1.add(1500);
		admissibleFrequencies1.add(3000);
		admissibleFrequencies1.add(4000);

		Map<Integer, Integer> processingPower1 = new HashMap<Integer, Integer>();

		// ProcessingPower
		processingPower1.put(1000, 1000000);
		processingPower1.put(1500, 1500000);
		processingPower1.put(3000, 3000000);
		processingPower1.put(4000, 4000000);
		Computer c1 = new Computer(computerURI1, admissibleFrequencies1, processingPower1, 3000, 4000,
				numberOfProcessors1, numberOfCores1, ComputerServicesInboundPortURI1,
				ComputerStaticStateDataInboundPortURI1, ComputerDynamicStateDataInboundPortURI1);
		this.addDeployedComponent(c1);

		// --------------------------------------------------------------------

		cores = new Hashtable<String, Integer>();
		cores.put(computerURI1, numberOfCores1 * numberOfProcessors1);
		cores.put(computerURI, numberOfCores * numberOfProcessors);
		Computeruriservice = new Hashtable<String, String>();
		Computeruriservice.put(computerURI, ComputerServicesInboundPortURI);
		Computeruriservice.put(computerURI1, ComputerServicesInboundPortURI1);

		ArrayList<String> inboundporturi = new ArrayList<String>();

		// Application 1

		this.app1 = new Application("app1", 2, 2, 5000.0, 60000000000L, RequestGeneratorManagementInboundPortURI,
				"rcdport1");
		this.addDeployedComponent(this.app1);

		// Application 2

		this.app2 = new Application("app2", 2, 2, 5000.0, 60000000000L, RequestGeneratorManagementInboundPortURI1,
				"rcdport2");
		this.addDeployedComponent(this.app2);

		inboundporturi.add("rcdport1");
		inboundporturi.add("rcdport2");

		// Computermonitor1

		this.computermonitor1 = new ComputerMonitor(computerURI, true, ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(this.computermonitor1);
		computermonitor1.toggleLogging();
		computermonitor1.toggleTracing();

		// Computermonitor2

		this.computermonitor2 = new ComputerMonitor(computerURI1, true, ComputerStaticStateDataInboundPortURI1,
				ComputerDynamicStateDataInboundPortURI1);
		this.addDeployedComponent(this.computermonitor2);
		computermonitor2.toggleLogging();
		computermonitor2.toggleTracing();

		ArrayList<String> applicationuri = new ArrayList<String>();

		applicationuri.add("app1");
		applicationuri.add("app2");

		// the upper bound of PerformanceController
		final double upperbound = 30.0;

		// the lower bound of PerformanceController

		final double lowerbound = 20.0;

		// in this example , the order of execution is the normal ( Core ->
		// VM Control -> Frequency )

		HashMap<strategy, decision> controlStatus = new HashMap<strategy, decision>();

		controlStatus.put(strategy.FREQUENCY_CONTROL, decision.IS_DECREASE);
		controlStatus.put(strategy.CORE_CONTROL, decision.NONE);
		controlStatus.put(strategy.VM_CONTROL, decision.NONE);

		AdmissionController admission;
		admission = new AdmissionController(this.cores, applicationuri, inboundporturi, Computeruriservice, lowerbound,
				upperbound, controlStatus);
		this.addDeployedComponent(admission);

		super.deploy();

	}

	public void start() throws Exception {
		super.start();

		this.app1.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {
					((Application) this.getOwner()).dynamicDeploy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app1.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {

					((Application) this.getOwner()).launch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app2.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {
					((Application) this.getOwner()).dynamicDeploy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app2.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {

					((Application) this.getOwner()).launch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#execute()
	 */

	/**
	 * execute the test application.
	 * 
	 * @param args
	 *            command line arguments, disregarded here.
	 */
	public static void main(String[] args) {
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestProject_3 trg = new TestProject_3();
			trg.startStandardLifeCycle(300000L);
			// Augment the time if you want to examine the traces after
			// the exeuction of the program.
			Thread.sleep(100000L);
			// Exit from Java (closes all trace windows...).
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
