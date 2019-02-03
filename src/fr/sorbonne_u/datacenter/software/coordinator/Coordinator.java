package fr.sorbonne_u.datacenter.software.coordinator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.datacenter.software.coordinator.interfaces.CoordinatorManagementI;
import fr.sorbonne_u.datacenter.software.coordinator.ports.CoordinatorServiceInboundPort;
import fr.sorbonne_u.datacenter.software.coordinator.ports.CoordinatorServiceOutboundport;
import fr.sorbonne_u.datacenterclient.utils.AveragePerformanceChart;

/**
 * The class <code>Coordinator</code> the coordinator manages the frequency
 * change coordination by the ApplicationVM using the same processor
 *
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 *
 * <p>
 * 
 * this component is used to synchronize between the different controls
 * performed by the PerfomranceController. It groups information about the
 * ApplicationVM that uses the same processor. At the time of the increase or
 * the decrease of the frequency, a control can violate the constraint of
 * difference between the frequencies of the cores of the same processor. in
 * order to solve the problem, it is necessary that the next control on the
 * processor is in the same direction as the preceding one in order to try to
 * solve the problem and not to increase the difference between the frequencies
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

public class Coordinator extends AbstractComponent implements CoordinatorManagementI {

	/** Decision of the Coordinator */

	public enum decision {
		NONE, INCREASE_FREQUENCY, DECREASE_FREQUENCY
	}

	/** CoordinatorURI */

	public String coordinatorURI;

	/** The real maxFrequencyGap used */

	public int maxFrequencyGap = 1000;

	/** Set of possible frequencies */

	Set<Integer> possibleFrequencies;

	/** HashMap List of processors per ApplicationVM */

	public HashMap<String, ArrayList<Integer>> processorPerAvm;

	/** HashMap of processors and decision */

	public HashMap<Integer, decision> CoordinationState;

	/** Core Frequency per Processor */

	public HashMap<Integer, ArrayList<Integer>> coreFrequencyPerProcessor;

	/** Coordinator Service Inbound Port */

	protected CoordinatorServiceInboundPort coordinatorServiceinboundport;

	/**
	 *
	 * The Constructor of the Coordinator
	 *
	 * @param coordinatorURI
	 *            CoordinatorURI
	 * @param maxFrequencyGap
	 *            URI of OutboundApplicationport
	 * @param possibleFrequencies
	 *            Possible frequency
	 * @param processnum
	 *            number of processors
	 * @param coreFrequencyPerProcessor
	 *            Frequency per processor
	 * @throws Exception
	 */

	public Coordinator(String coordinatorURI, int maxFrequencyGap, Set<Integer> possibleFrequencies, int processnum,
			HashMap<Integer, ArrayList<Integer>> coreFrequencyPerProcessor) throws Exception {
		super(2, 2);

		this.coordinatorURI = coordinatorURI;
		this.processorPerAvm = new HashMap<String, ArrayList<Integer>>();
		this.coreFrequencyPerProcessor = new HashMap<Integer, ArrayList<Integer>>();
		// this.maxFrequencyGap = maxFrequencyGap ;
		this.possibleFrequencies = possibleFrequencies;
		this.CoordinationState = new HashMap<Integer, decision>();

		for (int i = 0; i < processnum; i++) {
			this.CoordinationState.put(i, decision.NONE);
		}

		this.coreFrequencyPerProcessor = coreFrequencyPerProcessor;

		this.coordinatorServiceinboundport = new CoordinatorServiceInboundPort(this.coordinatorURI + "-inbound", this);
		this.addPort(coordinatorServiceinboundport);
		this.coordinatorServiceinboundport.publishPort();

		tracer.setRelativePosition(AveragePerformanceChart.positionX, AveragePerformanceChart.positionY);
		if (AveragePerformanceChart.positionY == 3) {
			AveragePerformanceChart.positionX++;
			AveragePerformanceChart.positionY = 0;
		} else
			AveragePerformanceChart.positionY++;

		tracer.setTitle(this.coordinatorURI);
		this.toggleTracing();
		this.toggleLogging();

	}

	/**
	 * 
	 * this method provides synchronization between the applicationVMs that use the
	 * same processors. at each control, the coordinator must check the decision of
	 * the processor. we have 3 general cases: the processor does not oblige a
	 * particular decision. in this case he can accept the new frequency. he must
	 * then check whether the new frequency has to respect the constraint or not. In
	 * case of violation the coordinator changes the decision in the same direction
	 * as the control for the next to respect the decision.
	 * 
	 * the second case, the processor forces a particular decision in the same
	 * direction as the control in this case, it verifies that the new frequency
	 * value can correct the constraint and it changes the frequency in this case.
	 * At the end, if there is a correction of the state of the processor, we can
	 * change the decision to NONE
	 * 
	 * the last case, the processor obliges a decision different from the direction
	 * of control in this case it is necessary to accept the decision and to modify
	 * the control according to the decision. At the end, if there is a correction
	 * of the state of the processor, we can change the decision to NONE
	 * 
	 * 
	 * @param frequencycore
	 *            Requested frequency
	 * @param processornum
	 *            Processor index
	 * @param core
	 *            Core index
	 * @param state
	 *            Increase or decrease
	 * 
	 * @return Frequency
	 */

	@Override
	public int ControlFrequency(int frequencycore, int processornum, int core, boolean state) throws Exception {

		// look if an other controller force to change frequency in the same way
		this.logMessage(coreFrequencyPerProcessor.toString());
		this.logMessage(CoordinationState.toString());

		ArrayList<Integer> corefrequency = new ArrayList<Integer>();
		corefrequency = coreFrequencyPerProcessor.get(processornum);

		// INCREASE CONTROL

		if (state == true) {

			// If the decision is NONE , we can change frequency

			if (this.CoordinationState.get(processornum).equals(decision.NONE)) {

				corefrequency.set(core, frequencycore);

				// Check the min and max Frequency

				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);

				// Force the nextcontrol to increase the frequency on processor

				if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {

					this.CoordinationState.replace(processornum, decision.INCREASE_FREQUENCY);

				} else {

					// the new frequency respect constraint

					this.CoordinationState.replace(processornum, decision.NONE);

				}

				return frequencycore;

			} else if (this.CoordinationState.get(processornum).equals(decision.INCREASE_FREQUENCY)) {

				// descision is INCREASE_FREQUENCY , the same with the control

				int frequency = corefrequency.get(core);
				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);

				// if the actual frequency is the min and we have constraint violation , we can
				// increase ( the new frequency can resolve problem )

				if (frequency == minCorefrequency && maxCorefrequency - frequency > maxFrequencyGap) {

					corefrequency.set(core, frequencycore);

					minCorefrequency = Collections.min(corefrequency);
					maxCorefrequency = Collections.max(corefrequency);

					// Force the nextcontrol to increase the frequency on processor

					if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {

						this.CoordinationState.replace(processornum, decision.INCREASE_FREQUENCY);

					} else {

						// the new frequency respect constraint

						this.CoordinationState.replace(processornum, decision.NONE);
					}

					return frequencycore;

				} else {

					return -1;
				}

			}

			// if decision is DECREASE_FREQUENCY (Different way than the control)

			else if (this.CoordinationState.get(processornum).equals(decision.DECREASE_FREQUENCY)) {

				// Deacrease the frequency

				int freqcore = corefrequency.get(core);
				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);
				boolean canDecrease = false;

				// the new value of frequency respect constraint with max and min

				for (Integer frequency : this.possibleFrequencies) {
					if (frequency < freqcore && frequency - minCorefrequency < maxFrequencyGap
							&& maxCorefrequency - frequency < maxFrequencyGap) {
						canDecrease = true;
						freqcore = frequency;
						corefrequency.set(core, frequency);
						break;
					}
				}

				if (canDecrease == true) {

					minCorefrequency = Collections.min(corefrequency);
					maxCorefrequency = Collections.max(corefrequency);

					// if the new value don't resolve problem

					if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {
						this.CoordinationState.replace(processornum, decision.DECREASE_FREQUENCY);
					} else {

						// if the new value resolve problem

						this.CoordinationState.replace(processornum, decision.NONE);
					}

					return freqcore;
				} else
					return -1;

			}
			return -1;
		}

		// DECREASE CONTROL
		else

		{
			if (this.CoordinationState.get(processornum).equals(decision.NONE)) {

				// If the decision is NONE , we can change frequency

				corefrequency.set(core, frequencycore);

				// Check the min and max Frequency

				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);

				// Force the nextcontrol to Decrease the frequency on processor

				if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {
					this.CoordinationState.replace(processornum, decision.DECREASE_FREQUENCY);
				} else {
					// the new frequency respect constraint
					this.CoordinationState.replace(processornum, decision.NONE);
				}
				return frequencycore;
			} else if (this.CoordinationState.get(processornum).equals(decision.DECREASE_FREQUENCY)) {

				// descision is DECREASE_FREQUENCY , the same with the control

				int frequency = corefrequency.get(core);
				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);

				// if the actual frequency is the max and we have constraint violation , we can
				// decrease ( the new frequency can resolve problem )

				if (frequency == maxCorefrequency && frequency - minCorefrequency > maxFrequencyGap) {

					corefrequency.set(core, frequencycore);

					minCorefrequency = Collections.min(corefrequency);
					maxCorefrequency = Collections.max(corefrequency);

					// Force the nextcontrol to Decrease the frequency on processor

					if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {

						this.CoordinationState.replace(processornum, decision.DECREASE_FREQUENCY);

					} else {

						// the new frequency respect constraint

						this.CoordinationState.replace(processornum, decision.NONE);
					}

					return frequencycore;

				} else {

					return -1;
				}

			}
			// if decision is INCREASE_FREQUENCY (Different way than the control)
			else {

				// Increase the frequency

				int freqcore = corefrequency.get(core);
				int minCorefrequency = Collections.min(corefrequency);
				int maxCorefrequency = Collections.max(corefrequency);
				boolean canDecrease = false;

				// the new value of frequency respect constraint with max and min

				for (Integer frequency : this.possibleFrequencies) {
					if (frequency > freqcore && frequency - minCorefrequency < maxFrequencyGap
							&& maxCorefrequency - frequency < maxFrequencyGap) {
						canDecrease = true;
						freqcore = frequency;
						corefrequency.set(core, frequency);
						break;
					}
				}

				if (canDecrease == true) {

					minCorefrequency = Collections.min(corefrequency);
					maxCorefrequency = Collections.max(corefrequency);

					// if the new value don't resolve problem
					if (maxCorefrequency - minCorefrequency > maxFrequencyGap) {
						this.CoordinationState.replace(processornum, decision.INCREASE_FREQUENCY);
						// the new frequency respect constraint
					} else {
						this.CoordinationState.replace(processornum, decision.NONE);
					}
					return freqcore;
				} else
					return -1;

			}
		}

	}

	/**
	 * This method update the List of the ApplicatioVM using processors in the
	 * computer
	 * 
	 * @param numProcessor
	 *            List of processors indice used by the VM
	 * @param vmURI
	 *            ApplicationVM URI
	 */
	@Override
	public void UpdateCoordinator(ArrayList<Integer> numProcessor, String vmURI) throws Exception {

		this.processorPerAvm.put(vmURI, numProcessor);

	}

	/**
	 * This method remove ApplicationVM from the Coordinator
	 * 
	 * 
	 * @param vmURI
	 *            ApplicationVM URI
	 */
	@Override
	public void removeVMfromCoordinator(String vmURI) throws Exception {

		this.processorPerAvm.remove(vmURI);

	}
}
