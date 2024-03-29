package fr.sorbonne_u.datacenter.hardware.computers;

// Copyright Jacques Malenfant, Sorbonne Universite.
// 
// Jacques.Malenfant@lip6.fr
// 
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
// 
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
// 
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
// 
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
// 
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.interfaces.DataOfferedI;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.TimeManagement;
import fr.sorbonne_u.datacenter.connectors.ControlledDataConnector;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStaticStateDataI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerDynamicStateDataInboundPort;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesInboundPort;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerStaticStateDataInboundPort;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI;
import fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorStaticStateI;
import fr.sorbonne_u.datacenter.hardware.processors.ports.ProcessorDynamicStateDataOutboundPort;
import fr.sorbonne_u.datacenter.hardware.processors.ports.ProcessorServicesOutboundPort;
import fr.sorbonne_u.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataOfferedI;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataRequiredI;
import fr.sorbonne_u.datacenter.interfaces.PushModeControllingI;
import fr.sorbonne_u.datacenter.software.coordinator.Coordinator;
import fr.sorbonne_u.datacenter.software.coordinator.connectors.CoordinatorConnector;
import fr.sorbonne_u.datacenter.software.coordinator.interfaces.CoordinatorManagementI;
import fr.sorbonne_u.datacenter.software.coordinator.ports.CoordinatorServiceOutboundport;
import fr.sorbonne_u.datacenterclient.utils.AveragePerformanceChart;

/**
 * The class <code>Computer</code> implements a component that represents a
 * computer in a data center.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * Computer components use static and dynamic data structures. The static data
 * structure represents the hardware itself modeled as components and objects:
 * processor components, their URI, their ports URI, ... Instead of creating
 * processors aside and then providing the components to the computer, the
 * computer component is in charge of creating its processors. Hence, several
 * parameters passed to the constructor are in fact used for the creation of the
 * processors. The dynamic data structure includes essentially the state of
 * reservation of the cores to be able to allocate them on request.
 * </p>
 * <p>
 * The computer component offers its baseline services through the interface
 * <code>ComputerServicesI</code>. It allows to obtain information about its
 * static state by offering the interface <code>ComputerStaticStateDataI</code>,
 * which is a simple subinterface of the standard component interface
 * <code>DataOfferedI</code>, and thus offers its pull interface and requires
 * its push one. Similarly, it allows to obtain information about its dynamic
 * state by offering the interface <code>ComputerDynamicStateDataI</code>, which
 * is also a subinterface of the standard component interface
 * <code>DataOfferedI</code>, thus also offering its pull interface and
 * requiring its push one, but adds a few methods to start and stop the pushing
 * of dynamic data towards a monitoring component.
 * </p>
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * TODO: complete!
 * 
 * <pre>
 * invariant		computerURI != null
 * invariant		numberOfProcessors &gt; 0
 * invariant		numberOfCores &gt; 0
 * invariant		processors != null and processors.length == numberOfProcessors
 * invariant		processorStaticDataOutboundPorts != null and
 *				    processorStaticDataOutboundPorts.length == numberOfProcessors
 * invariant		processorDynamicDataOutboundPorts != null and
 *				    processorDynamicDataOutboundPorts.length == numberOfProcessors
 * invariant		processorsURI != null and
 *				    processorsURI.size() == numberOfProcessors
 * invariant		computerServicesInboundPortURI != null
 * invariant		computerStaticStateDataInboundPortURI != null
 * invariant		computerDynamicStateDataInboundPortURI != null
 * </pre>
 * 
 * <p>
 * Created on : January 15, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public class Computer extends AbstractComponent implements ProcessorStateDataConsumerI, PushModeControllingI {
	/** The three types of interfaces offered by Computer. */
	public static enum ComputerPortTypes {
		SERVICES, // basic services: allocating and releasing cores
		STATIC_STATE, // notification (data interface) for the static state
		DYNAMIC_STATE // notification (data interface) for the dynamic state
	}

	// ------------------------------------------------------------------------
	// Component public inner classes
	// ------------------------------------------------------------------------

	/**
	 * The class <code>AllocatedCore</code> implements object collecting information
	 * about cores allocated to an Application VM.
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
	 * invariant		processorURI != null
	 * invariant		processorNo &gt;= 0 and coreNo &gt;= 0
	 * invariant		processorInboundPortURI != null
	 * invariant		forall uri in processorInboundPortURI.values() {
	 * 				    uri != null
	 * 				}
	 * </pre>
	 * 
	 * <p>
	 * Created on : August 26, 2015
	 * </p>
	 * 
	 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class AllocatedCore implements Serializable {
		private static final long serialVersionUID = 1L;
		/** the number of the owning processor within its computer. */
		public final int processorNo;
		/** the URI of the owning processor within its computer. */
		public final String processorURI;
		/** the number of the core within its owning processor. */
		public final int coreNo;
		/**
		 * a map from processor port types and their URI for the owning processor.
		 */
		public final Map<Processor.ProcessorPortTypes, String> processorInboundPortURI;

		/**
		 * Creating a structure representing an allocated core.
		 * 
		 * <p>
		 * <strong>Contract</strong>
		 * </p>
		 * 
		 * <pre>
		 * pre	processorURI != null
		 * pre	processorNo &gt;= 0 and coreNo &gt;= 0
		 * pre	processorInboundPortURI != null
		 * pre	forall uri in processorInboundPortURI.values() {
		 * 		    uri != null
		 * 		}
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param processorNo
		 *            processor number within the computer.
		 * @param processorURI
		 *            URI of the processor.
		 * @param coreNo
		 *            core number within the processor.
		 * @param processorInboundPortURI
		 *            map giving the URIs of the inbound ports of the processors.
		 */
		public AllocatedCore(int processorNo, String processorURI, int coreNo,
				Map<ProcessorPortTypes, String> processorInboundPortURI) {
			super();

			assert processorURI != null;
			assert processorNo >= 0 && coreNo >= 0;
			assert processorInboundPortURI != null;
			boolean allNonNull = true;
			for (String uri : processorInboundPortURI.values()) {
				allNonNull = allNonNull && uri != null;
			}
			assert allNonNull;

			this.processorNo = processorNo;
			this.processorURI = processorURI;
			this.coreNo = coreNo;
			this.processorInboundPortURI = processorInboundPortURI;
		}
	}

	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------

	/** URI of the computer component. */
	protected final String computerURI;
	/** the number of processor owned by the computer. */
	protected final int numberOfProcessors;
	/** references to the owned processor components for internal usage. */
	protected final Processor[] processors;
	/**
	 * ports of the computer receiving the static data from its processor
	 * components.
	 */
	protected final ProcessorStaticStateDataOutboundPort[] processorStaticDataOutboundPorts;
	/**
	 * ports of the computer receiving the dynamic data from its processor
	 * components.
	 */
	protected final ProcessorDynamicStateDataOutboundPort[] processorDynamicDataOutboundPorts;
	/** number of cores of each processor (processor are core homogeneous). */
	protected final int numberOfCores;
	/** a map from processor numbers to processors URI. */
	protected final Map<Integer, String> processorsURI;
	/** a map from processor URI to their different inbound ports URI. */
	protected final Map<String, Map<Processor.ProcessorPortTypes, String>> processorsInboundPortURI;
	/** array collecting the reservation status of the cores. */
	protected boolean[][] reservedCores;
	/** computer inbound port through which management methods are called. */
	protected ComputerServicesInboundPort computerServicesInboundPort;
	/** computer data inbound port through which it pushes its static data. */
	protected ComputerStaticStateDataInboundPort computerStaticStateDataInboundPort;
	/** computer data inbound port through which it pushes its dynamic data. */
	protected ComputerDynamicStateDataInboundPort computerDynamicStateDataInboundPort;
	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;

	protected Coordinator computercoordinator;

	protected CoordinatorServiceOutboundport coordinatorServiceOutboundPort;

	// ------------------------------------------------------------------------
	// Component constructor
	// ------------------------------------------------------------------------

	/**
	 * create a computer component.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	computerURI != null
	 * pre	possibleFrequencies != null and forall i in possibleFrequencies, i &gt; 0
	 * pre	processingPower != null and forall i in processingPower.values(), i &gt; 0
	 * pre	processingPower.keySet().containsAll(possibleFrequencies)
	 * pre	possibleFrequencies.contains(defaultFrequency)
	 * pre	maxFrequencyGap &gt;= 0 and forall i in possibleFrequencies, maxFrequencyGap &lt;= i
	 * pre	numberOfProcessors &gt; 0
	 * pre	numberOfCores &gt; 0
	 * pre	computerServicesInboundPortURI != null
	 * pre	computerStaticStateDataInboundPortURI != null
	 * pre	computerDynamicStateDataInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param computerURI
	 *            URI of the computer.
	 * @param possibleFrequencies
	 *            possible frequencies for cores.
	 * @param processingPower
	 *            Mips for the different possible frequencies.
	 * @param defaultFrequency
	 *            default frequency at which the cores run.
	 * @param maxFrequencyGap
	 *            max frequency gap among cores of the same processor.
	 * @param numberOfProcessors
	 *            number of processors in the computer.
	 * @param numberOfCores
	 *            number of cores per processor (homogeneous).
	 * @param computerServicesInboundPortURI
	 *            URI of the computer service inbound port.
	 * @param computerStaticStateDataInboundPortURI
	 *            URI of the computer static data notification inbound port.
	 * @param computerDynamicStateDataInboundPortURI
	 *            URI of the computer dynamic data notification inbound port.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public Computer(String computerURI, Set<Integer> possibleFrequencies, Map<Integer, Integer> processingPower,
			int defaultFrequency, int maxFrequencyGap, int numberOfProcessors, int numberOfCores,
			String computerServicesInboundPortURI, String computerStaticStateDataInboundPortURI,
			String computerDynamicStateDataInboundPortURI) throws Exception {
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(computerURI, 1, 1);

		// Verifying the preconditions
		assert computerURI != null;
		assert possibleFrequencies != null;
		boolean allPositive = true;
		for (int f : possibleFrequencies) {
			allPositive = allPositive && (f > 0);
		}
		assert allPositive;
		assert processingPower != null;
		allPositive = true;
		for (int ips : processingPower.values()) {
			allPositive = allPositive && ips > 0;
		}
		assert allPositive;
		assert processingPower.keySet().containsAll(possibleFrequencies);
		assert possibleFrequencies.contains(defaultFrequency);
		int max = -1;
		for (int f : possibleFrequencies) {
			if (max < f) {
				max = f;
			}
		}
		assert maxFrequencyGap >= 0 && maxFrequencyGap <= max;
		assert numberOfProcessors > 0;
		assert numberOfCores > 0;
		assert computerServicesInboundPortURI != null;
		assert computerStaticStateDataInboundPortURI != null;
		assert computerDynamicStateDataInboundPortURI != null;

		// For processor static data
		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);

		this.computerURI = computerURI;
		this.numberOfProcessors = numberOfProcessors;
		this.numberOfCores = numberOfCores;
		this.processors = new Processor[numberOfProcessors];
		this.processorStaticDataOutboundPorts = new ProcessorStaticStateDataOutboundPort[numberOfProcessors];
		this.processorDynamicDataOutboundPorts = new ProcessorDynamicStateDataOutboundPort[numberOfProcessors];
		this.processorsURI = new HashMap<Integer, String>();
		this.processorsInboundPortURI = new HashMap<String, Map<Processor.ProcessorPortTypes, String>>();
		// Create the different processors
		for (int i = 0; i < numberOfProcessors; i++) {
			// generate URI for the processor and its different ports
			String processorURI = this.computerURI + "-processor-" + i;
			String psibpURI = processorURI + "-psibp";
			String piibpURI = processorURI + "-piibp";
			String pmibpURI = processorURI + "-pmibp";
			String pssdibpURI = processorURI + "-pssdibp";
			String pdsdibpURI = processorURI + "-pdsdibp";

			// record the mapping between the processor number and its generated
			// URI
			this.processorsURI.put(i, processorURI);
			// create the processor component
			this.processors[i] = new Processor(processorURI, possibleFrequencies, processingPower, defaultFrequency,
					maxFrequencyGap, numberOfCores, psibpURI, piibpURI, pmibpURI, pssdibpURI, pdsdibpURI);
			// add it to the deployed components in the CVM
			AbstractCVM.getCVM().addDeployedComponent(this.processors[i]);

			// create a map between the port types and the ports URI
			EnumMap<Processor.ProcessorPortTypes, String> map = new EnumMap<Processor.ProcessorPortTypes, String>(
					Processor.ProcessorPortTypes.class);
			map.put(Processor.ProcessorPortTypes.SERVICES, psibpURI);
			map.put(Processor.ProcessorPortTypes.INTROSPECTION, piibpURI);
			map.put(Processor.ProcessorPortTypes.MANAGEMENT, pmibpURI);
			map.put(Processor.ProcessorPortTypes.STATIC_STATE, pssdibpURI);
			map.put(Processor.ProcessorPortTypes.DYNAMIC_STATE, pdsdibpURI);
			// record this map for the processor in the computer data
			this.processorsInboundPortURI.put(processorURI, map);

			// create the computer ports to receive the static and dynmaic data
			// from the processor
			this.processorStaticDataOutboundPorts[i] = new ProcessorStaticStateDataOutboundPort(this, processorURI);
			this.addPort(this.processorStaticDataOutboundPorts[i]);
			this.processorStaticDataOutboundPorts[i].publishPort();
			this.doPortConnection(this.processorStaticDataOutboundPorts[i].getPortURI(), pssdibpURI,
					DataConnector.class.getCanonicalName());

			this.processorDynamicDataOutboundPorts[i] = new ProcessorDynamicStateDataOutboundPort(this, processorURI);
			this.addPort(this.processorDynamicDataOutboundPorts[i]);
			this.processorDynamicDataOutboundPorts[i].publishPort();
			this.doPortConnection(this.processorDynamicDataOutboundPorts[i].getPortURI(), pdsdibpURI,
					ControlledDataConnector.class.getCanonicalName());
		}

		// Initialize the reservation status of the cores.
		this.reservedCores = new boolean[this.numberOfProcessors][this.numberOfCores];
		for (int np = 0; np < this.numberOfProcessors; np++) {
			for (int nc = 0; nc < this.numberOfCores; nc++) {
				this.reservedCores[np][nc] = false;
			}
		}

		// Adding computer interfaces, creating and publishing the related ports
		this.addOfferedInterface(ComputerServicesI.class);
		this.computerServicesInboundPort = new ComputerServicesInboundPort(computerServicesInboundPortURI, this);
		this.addPort(this.computerServicesInboundPort);
		this.computerServicesInboundPort.publishPort();

		this.addOfferedInterface(DataOfferedI.PullI.class);
		this.addRequiredInterface(DataOfferedI.PushI.class);
		this.addOfferedInterface(ComputerStaticStateDataI.class);
		this.computerStaticStateDataInboundPort = new ComputerStaticStateDataInboundPort(
				computerStaticStateDataInboundPortURI, this);
		this.addPort(this.computerStaticStateDataInboundPort);
		this.computerStaticStateDataInboundPort.publishPort();

		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.computerDynamicStateDataInboundPort = new ComputerDynamicStateDataInboundPort(
				computerDynamicStateDataInboundPortURI, this);
		this.addPort(computerDynamicStateDataInboundPort);
		this.computerDynamicStateDataInboundPort.publishPort();

		HashMap<Integer, ArrayList<Integer>> coreFrequencyPerProcessor;
		coreFrequencyPerProcessor = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < this.processors.length; i++) {
			ArrayList<Integer> corepross = new ArrayList<Integer>();
			for (int j = 0; j < this.processors[i].getCores().length; j++) {
				corepross.add(this.processors[i].getCores()[j].getCurrentFrequency());
			}
			coreFrequencyPerProcessor.put(i, corepross);
		}

		this.addOfferedInterface(CoordinatorManagementI.class);
		this.coordinatorServiceOutboundPort = new CoordinatorServiceOutboundport(this);
		this.addPort(coordinatorServiceOutboundPort);
		this.coordinatorServiceOutboundPort.publishPort();

		computercoordinator = new Coordinator(this.computerURI + "coordinator", maxFrequencyGap, possibleFrequencies,
				numberOfProcessors, coreFrequencyPerProcessor);

		this.doPortConnection(coordinatorServiceOutboundPort.getPortURI(), this.computerURI + "coordinator-inbound",
				CoordinatorConnector.class.getCanonicalName());

	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {
		for (int i = 0; i < this.numberOfProcessors; i++) {
			try {
				// disconnect the ports between the computer and its processors
				if (this.processorStaticDataOutboundPorts[i].connected()) {
					this.processorStaticDataOutboundPorts[i].doDisconnection();
				}
				if (this.processorDynamicDataOutboundPorts[i].connected()) {
					this.processorDynamicDataOutboundPorts[i].doDisconnection();
				}
				// disconnect the ports between the computer and its clients
				if (this.computerStaticStateDataInboundPort.connected()) {
					this.computerStaticStateDataInboundPort.doDisconnection();
				}
				if (this.computerDynamicStateDataInboundPort.connected()) {
					this.computerDynamicStateDataInboundPort.doDisconnection();
				}
			} catch (Exception e) {
				throw new ComponentShutdownException(e);
			}
		}
		super.finalise();
	}

	/**
	 * shutdown the computer, first disconnecting all processor components' outbound
	 * ports.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		try {
			for (int i = 0; i < this.numberOfProcessors; i++) {
				this.processorStaticDataOutboundPorts[i].unpublishPort();
				this.processorDynamicDataOutboundPorts[i].unpublishPort();
			}
			this.computerServicesInboundPort.unpublishPort();
			this.computerStaticStateDataInboundPort.unpublishPort();
			this.computerDynamicStateDataInboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	public void execute() throws Exception {
		// this.processorDynamicDataOutboundPorts[0].startLimitedPushing(1000, 25);
	}

	/**
	 * toggle logging for the computer component and its processor components.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#toggleLogging()
	 */
	@Override
	public void toggleLogging() {
		for (int p = 0; p < this.numberOfProcessors; p++) {
			this.processors[p].toggleLogging();
		}
		super.toggleLogging();
	}

	/**
	 * toggle tracing for the computer component and its processor components.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.components.AbstractComponent#toggleTracing()
	 */
	@Override
	public void toggleTracing() {
		for (int p = 0; p < this.numberOfProcessors; p++) {
			this.processors[p].toggleTracing();
		}
		super.toggleTracing();
	}

	// ------------------------------------------------------------------------
	// Component introspection services (ComputerStaticStateDataI)
	// ------------------------------------------------------------------------

	/**
	 * collect and return the static state of the computer.
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
	 * @return the static state of the computer.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public ComputerStaticStateI getStaticState() throws Exception {
		Map<Integer, String> pURIs = new HashMap<Integer, String>(this.processorsURI.size());
		Map<String, Map<Processor.ProcessorPortTypes, String>> pPortsURI = new HashMap<String, Map<Processor.ProcessorPortTypes, String>>(
				this.processorsURI.size());
		for (Integer n : this.processorsURI.keySet()) {
			pURIs.put(n, this.processorsURI.get(n));
			Map<Processor.ProcessorPortTypes, String> pIbpURIs = new HashMap<Processor.ProcessorPortTypes, String>();
			for (Processor.ProcessorPortTypes ppt : this.processorsInboundPortURI.get(this.processorsURI.get(n))
					.keySet()) {
				pIbpURIs.put(ppt, this.processorsInboundPortURI.get(this.processorsURI.get(n)).get(ppt));
			}
			pPortsURI.put(this.processorsURI.get(n), pIbpURIs);
		}
		return new ComputerStaticState(this.computerURI, this.numberOfProcessors, this.numberOfCores, pURIs, pPortsURI);
	}

	/**
	 * push the static state of the computer through its notification data inbound
	 * port.
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
	public void sendStaticState() throws Exception {
		if (this.computerStaticStateDataInboundPort.connected()) {
			ComputerStaticStateI css = this.getStaticState();
			this.computerStaticStateDataInboundPort.send(css);
		}
	}

	// ------------------------------------------------------------------------
	// Component introspection services
	// ------------------------------------------------------------------------

	/**
	 * collect and return the dynamic state of the computer.
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
	 * @return the dynamic state of the computer.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public ComputerDynamicStateI getDynamicState() throws Exception {
		return new ComputerDynamicState(this.computerURI, this.reservedCores);
	}

	/**
	 * push the dynamic state of the computer through its notification data inbound
	 * port.
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
	public void sendDynamicState() throws Exception {
		if (this.computerDynamicStateDataInboundPort.connected()) {
			ComputerDynamicStateI cds = this.getDynamicState();
			this.computerDynamicStateDataInboundPort.send(cds);
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
						((Computer) this.getOwner()).sendDynamicState(interval, fNumberOfRemainingPushes);
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
	@Override
	public void startUnlimitedPushing(int interval) throws Exception {
		// first, send the static state if the corresponding port is connected
		this.sendStaticState();

		this.pushingFuture = this.scheduleTaskAtFixedRate(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((Computer) this.getOwner()).sendDynamicState();
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
	@Override
	public void startLimitedPushing(final int interval, final int n) throws Exception {
		assert n > 0;

		this.logMessage(
				this.computerURI + " startLimitedPushing with interval " + interval + " ms for " + n + " times.");

		// first, send the static state if the corresponding port is connected
		this.sendStaticState();

		this.pushingFuture = this.scheduleTask(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((Computer) this.getOwner()).sendDynamicState(interval, n);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, TimeManagement.acceleratedDelay(interval), TimeUnit.MILLISECONDS);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.interfaces.PushModeControllingI#stopPushing()
	 */
	@Override
	public void stopPushing() throws Exception {
		if (this.pushingFuture != null && !(this.pushingFuture.isCancelled() || this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false);
		}
	}

	// ------------------------------------------------------------------------
	// Component self-monitoring (ProcessorStateDataConsumerI)
	// ------------------------------------------------------------------------

	/**
	 * process the static state data received from a processor.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	processorURI != null and processorsURI.containsValue(processorURI)
	 * pre	ss != null
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI#acceptProcessorStaticData(java.lang.String,
	 *      fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorStaticStateI)
	 */
	@Override
	public void acceptProcessorStaticData(String processorURI, ProcessorStaticStateI ss) throws Exception {
		assert processorURI != null && processorsURI.containsValue(processorURI);
		assert ss != null;

		this.logMessage("Computer " + this.computerURI + " accepting static data from " + processorURI);
		this.logMessage("  timestamp              : " + ss.getTimeStamp());
		this.logMessage("  timestamper id         : " + ss.getTimeStamperId());
		this.logMessage("  number of cores        : " + ss.getNumberOfCores());
		this.logMessage("  default frequency      : " + ss.getDefaultFrequency());
		this.logMessage("  max. frequency gap     : " + ss.getMaxFrequencyGap());
		String adfreq = "  admissible frequencies : [";
		int count = ss.getAdmissibleFrequencies().size();
		for (Integer f : ss.getAdmissibleFrequencies()) {
			adfreq += "" + f;
			count--;
			if (count > 0) {
				adfreq += ", ";
			}
		}
		adfreq += "]";
		this.logMessage(adfreq);
		String pp = "  processing power       : [";
		count = ss.getProcessingPower().entrySet().size();
		for (Entry<Integer, Integer> e : ss.getProcessingPower().entrySet()) {
			pp += "(" + e.getKey() + " => " + e.getValue() + ")";
			count--;
			if (count > 0) {
				pp += ", ";
			}
		}
		this.logMessage(pp + "]");
	}

	/**
	 * process the dynamic state data received from a processor.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	processorURI != null and processorsURI.containsValue(processorURI)
	 * pre	cds != null
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI#acceptProcessorDynamicData(java.lang.String,
	 *      fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI)
	 */
	@Override
	public void acceptProcessorDynamicData(String processorURI, ProcessorDynamicStateI cds) throws Exception {

		assert processorURI != null && processorsURI.containsValue(processorURI);
		assert cds != null;
		this.logMessage("Computer " + this.computerURI + " accepting dynamic data from " + processorURI);
		this.logMessage("  timestamp                : " + cds.getTimeStamp());
		this.logMessage("  timestamper id           : " + cds.getTimeStamperId());
		String idleStatus = "  current idle status      : [";
		for (int i = 0; i < cds.getCoresIdleStatus().length; i++) {
			idleStatus += cds.getCoreIdleStatus(i);
			if (i < cds.getCoresIdleStatus().length - 1) {
				idleStatus += ", ";
			}
		}
		this.logMessage(idleStatus + "]");
		String coreFreq = "  current core frequencies : [";
		for (int i = 0; i < cds.getCurrentCoreFrequencies().length; i++) {
			coreFreq += cds.getCurrentCoreFrequency(i);
			if (i < cds.getCurrentCoreFrequencies().length - 1) {
				coreFreq += ", ";
			}
		}
		this.logMessage(coreFreq + "]");
	}

	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	/**
	 * allocate one core on this computer and return an instance of
	 * <code>AllocatedCore</code> containing the processor number, the core number
	 * and a map giving the URI of the processor inbound ports; return null if no
	 * core is available.
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
	 * @return an instance of <code>AllocatedCore</code> with the data about the
	 *         allocated core.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public AllocatedCore allocateCore() throws Exception {

		AllocatedCore ret = null;
		int processorNo = -1;
		int coreNo = -1;
		boolean notFound = true;
		for (int p = 0; notFound && p < this.numberOfProcessors; p++) {
			for (int c = 0; notFound && c < this.numberOfCores; c++) {
				if (!this.reservedCores[p][c]) {
					notFound = false;
					this.reservedCores[p][c] = true;
					processorNo = p;
					coreNo = c;
				}
			}
		}
		if (!notFound) {
			ret = new AllocatedCore(processorNo, this.processorsURI.get(processorNo), coreNo,
					this.processorsInboundPortURI.get(this.processorsURI.get(processorNo)));
		}
		return ret;
	}

	/**
	 * 
	 * deallocate the cores of a VM application. This method receives in parameter
	 * an AllocatedCore [] in order to know the cores and the processors already
	 * reserved.
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
	 * @param vm
	 *            AllocatedCore[] in order to know the cores and the processors
	 *            already reserved
	 * @throws Exception
	 * 
	 */

	public void DeallocateCores(AllocatedCore[] vm, String vmURI) throws Exception {

		this.traceMessage("****** Ready to deallocate cores ***** ");

		// Release AllocatedCores

		for (int i = 0; i < vm.length; i++) {
			int processorIndice = vm[i].processorNo;
			int core = vm[i].coreNo;
			this.reservedCores[processorIndice][core] = false;
		}

		this.coordinatorServiceOutboundPort.removeVMfromCoordinator(vmURI);
	}

	/**
	 * 
	 * 
	 * This method is called in order to update the computer cores status
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
	 * 
	 * @throws Exception
	 * 
	 */

	public int getAvailableCoresnumber() {
		int availablecoresnumber = 0;
		for (int i = 0; i < this.reservedCores.length; i++) {
			for (int j = 0; j < this.reservedCores[i].length; j++) {
				if (reservedCores[i][j] == false) {
					availablecoresnumber++;
				}
			}
		}

		return availablecoresnumber;
	}

	/**
	 * allocate up to <code>numberRequested</code> cores on this computer and return
	 * and array of <code>AllocatedCore</code> containing the data for each
	 * requested core; return an empty array if no core is available.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	numberRequested &gt; 0
	 * post	return.length &gt;= 0 and return.length &lt;= numberRequested
	 * </pre>
	 *
	 * @param numberRequested
	 *            number of cores to be allocated.
	 * @return an array of instances of <code>AllocatedCore</code> with the data
	 *         about the allocated cores.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public AllocatedCore[] allocateCores(int numberRequested) throws Exception {
		Vector<AllocatedCore> allocated = new Vector<AllocatedCore>(numberRequested);
		boolean notExhausted = true;
		for (int i = 0; notExhausted && i < numberRequested; i++) {
			AllocatedCore c = this.allocateCore();
			if (c != null) {
				allocated.add(c);
			} else {
				notExhausted = false;
			}
		}
		return allocated.toArray(new AllocatedCore[0]);
	}

	/**
	 * releases a priorly reserved core.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	this.isReserved(ac.processorNo, ac.coreNo) ;
	 * post	!this.isReserved(ac.processorNo, ac.coreNo) ;
	 * </pre>
	 *
	 * @param ac
	 *            priorly allocated core data.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void releaseCore(AllocatedCore ac) throws Exception {
		assert this.isReserved(ac.processorNo, ac.coreNo);

		this.reservedCores[ac.processorNo][ac.coreNo] = false;

		assert !this.isReserved(ac.processorNo, ac.coreNo);
	}

	/**
	 * release an array of priorly reserved cores.
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
	 * @param acs
	 *            array of priorly allocated cores data.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public void releaseCores(AllocatedCore[] acs) throws Exception {
		for (int i = 0; i < acs.length; i++) {
			this.releaseCore(acs[i]);
		}
	}

	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------

	/**
	 * reserve the core <code>coreNo</code> of processor <code>processorNo</code>.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	!this.isReserved(processorNo, coreNo)
	 * post	this.isReserved(processorNo, coreNo)
	 * </pre>
	 *
	 * @param processorNo
	 *            number of the processor.
	 * @param coreNo
	 *            number of the core.
	 * @throws Exception
	 *             when the core is already reserved.
	 */
	public void reserveCore(int processorNo, int coreNo) throws Exception {
		assert !this.isReserved(processorNo, coreNo);

		this.reservedCores[processorNo][coreNo] = true;

		assert this.isReserved(processorNo, coreNo);
	}

	/**
	 * return true if the core <code>coreNo</code> of processor
	 * <code>processorNo</code> is reserved and false otherwise.
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
	 * @param processorNo
	 *            number of the processor on which is the core to be tested.
	 * @param coreNo
	 *            number of the core to be tested.
	 * @return true if the core is reserved, false otherwise.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public boolean isReserved(int processorNo, int coreNo) throws Exception {
		return this.reservedCores[processorNo][coreNo];
	}

	/**
	 * utility to format processor information in a string for later logging or
	 * printing.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	leadingBlanks &gt;= 0
	 * pre	numberOfProcessors &gt; 0
	 * pre	processorsURI != null
	 * pre	processorsInboundPortURI != null
	 * pre	processorsURI.size() == processorsInboundPortURI.size()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param leadingBlanks
	 *            number of blank characters leading each line.
	 * @param numberOfProcessors
	 *            number of processors in the computer.
	 * @param processorsURI
	 *            URI of the processors.
	 * @param processorsInboundPortURI
	 *            map form processors' URI to URI of the processors' inbound ports.
	 * @return a string with preformatted information.
	 */
	public static String printProcessorsInboundPortURI(int leadingBlanks, int numberOfProcessors,
			Map<Integer, String> processorsURI,
			Map<String, Map<Processor.ProcessorPortTypes, String>> processorsInboundPortURI) {
		assert leadingBlanks >= 0;
		assert numberOfProcessors > 0;
		assert processorsURI != null;
		assert processorsInboundPortURI != null;
		assert processorsURI.size() == processorsInboundPortURI.size();

		StringBuffer sb = new StringBuffer();
		String leading = "";
		for (int i = 0; i < leadingBlanks; i++) {
			leading += " ";
		}
		for (int p = 0; p < processorsURI.size(); p++) {
			sb.append(leading + processorsURI.get(p) + "\n");
			Map<Processor.ProcessorPortTypes, String> pURIs = processorsInboundPortURI.get(processorsURI.get(p));
			for (Processor.ProcessorPortTypes pt : pURIs.keySet()) {
				sb.append(leading + "    " + pt + "  " + pURIs.get(pt) + "\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * this method is used to increase the frequency of the cores. AllocatedCore[]
	 * is used to know the processor and the cores that request the increase of the
	 * frequency. the new frequency must be in the admissible frequencies and must
	 * not have a large difference between the frequencies of the cores of the same
	 * processor. this method uses a coordinator that allows to coordinate between
	 * all the applications that share the same processor
	 * 
	 * @param allocatedcore
	 *            AllocatedCore[] in order to know the cores for frequency increase
	 * @return status of IncreaseFrequency
	 * @throws Exception
	 */

	public boolean IncreaseFrequency(AllocatedCore[] allocatedcore) throws Exception {

		boolean canIncrease = false;
		int newfrequency = 0;
		boolean result = false ; 
		
		ArrayList<Integer> frequencyList = new ArrayList<Integer>();

		for (int i = 0; i < allocatedcore.length; i++) {
			canIncrease = false;
			frequencyList = new ArrayList<Integer>();
			int processornum = allocatedcore[i].processorNo;
			int corenum = allocatedcore[i].coreNo;

			// Getting CurrentFrequency

			int FrequencyDefault = this.processors[processornum].getCores()[corenum].getCurrentFrequency();

			// Admissible Frequency

			for (Integer frequency : this.processors[processornum].getAdmissibleFrequencies()) {

			
				if (frequency > FrequencyDefault) {

					frequencyList.add(frequency);
					canIncrease = true;
				}
			}

			
			
			if(!frequencyList.isEmpty()) newfrequency = Collections.min(frequencyList);
			else newfrequency = 0 ;
			
			if (newfrequency > 0) {

				int oldfrequency = newfrequency;

				// Verify frequency in the coordinator ( Possibility for change or decrease
				// frequency )

				newfrequency = this.coordinatorServiceOutboundPort.ControlFrequency(newfrequency, processornum, corenum,
						true);

				
				
				if (newfrequency == -1 || newfrequency < oldfrequency) {

					
					// If the coordinator change the frequency in the different way
					
					this.logMessage("*** Coordinator Change Frequency ***");
					canIncrease = false;
				}

				if (canIncrease == true && newfrequency > 0) {
					this.logMessage("**** can increase frequency  ****");
					
					// Set new frequency

					this.processors[processornum].setCoreFrequency(corenum, newfrequency);
				} else {

					this.logMessage("***Cannot Increase frequency ***");
				}
			}
		}

		return canIncrease;

	}

	/**
	 * 
	 * this method is used to decrease the frequency of the cores. allocatedCore[]
	 * is used to know the processor and the cores that request the decrease of the
	 * frequency. the new frequency must be in the admissible frequencies and must
	 * not have a large difference between the frequencies of the cores of the same
	 * processor. this method uses a coordinator that allows to coordinate between
	 * all the applications that share the same processor
	 * 
	 * @param allocatedcore
	 *            AllocatedCore[] in order to know the cores for frequency increase
	 * @return status of DecreaseFrequency
	 * @throws Exception
	 */

	public boolean DecreaseFrequency(AllocatedCore[] allocatedcore) throws Exception {

		boolean canDecrease = false;
		boolean result = false ; 
		int newfrequency = 0;
		
		ArrayList<Integer> frequencyList = new ArrayList<Integer>();

		for (int i = 0; i < allocatedcore.length; i++) {
			canDecrease = false;
			frequencyList = new ArrayList<Integer>();

			int processornum = allocatedcore[i].processorNo;
			int corenum = allocatedcore[i].coreNo;

			// Getting CurrentFrequency

			int FrequencyDefault = this.processors[processornum].getCores()[corenum].getCurrentFrequency();

			for (Integer frequency : this.processors[processornum].getAdmissibleFrequencies()) {

				// Admissible Frequency

				if (frequency < FrequencyDefault) {
					
					frequencyList.add(frequency);
					canDecrease = true;

				}
			}

			// Verify frequency in the coordinator ( Possibility for change or increase
			// frequency )
			
			newfrequency = Collections.max(frequencyList);

			if (newfrequency > 0) {

				int oldfrequency = newfrequency;

				newfrequency = this.coordinatorServiceOutboundPort.ControlFrequency(newfrequency, processornum, corenum,
						false);

				if (newfrequency == -1 || newfrequency > oldfrequency) {

					// If the coordinator change the frequency in the different way

					this.logMessage("*** Coordinator Change Frequency ***");
					
					canDecrease = false;
				}

				if (canDecrease == true && newfrequency > 0) {

					this.logMessage("**** Can decrease frequency  ****");
					// Set new frequency
					this.processors[processornum].setCoreFrequency(corenum, newfrequency);
				} else {

					this.logMessage("**** Cannot decrease frequency  ****");
				}

			}
		}

		return canDecrease;

	}

	/**
	 * 
	 * updateCoordinator update the Coordinator list of ApplicationVM , the
	 * Coordinator have information about ApplicationVM which shares processors of
	 * the computer to coordinate between Applications
	 * 
	 * @param allocatedcore
	 *            AllocatedCore[] in order to know the cores for frequency increase
	 * @param uriVM
	 *            ApplicationVM URI
	 * @throws Exception
	 */

	public void updateCoordinator(AllocatedCore[] allocatedCores, String uriVM) throws Exception {
		ArrayList<Integer> processnum = new ArrayList<Integer>();

		// update Coordinator
		for (int i = 0; i < allocatedCores.length; i++) {
			processnum.add(allocatedCores[i].processorNo);
		}

		this.coordinatorServiceOutboundPort.UpdateCoordinator(processnum, uriVM);

	}

}
