package fr.sorbonne_u.datacenter.software.coordinator.interfaces;

import java.util.ArrayList;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

public interface CoordinatorManagementI extends		OfferedI,
RequiredI {
	
	
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
	public int ControlFrequency(int frequencycore, int processornum, int core, boolean state) throws Exception ;
	
	/**
	 * This method update the List of the ApplicatioVM using processors in the
	 * computer
	 * 
	 * @param numProcessor
	 *            List of processors indice used by the VM
	 * @param vmURI
	 *            ApplicationVM URI
	 */
	
	public void UpdateCoordinator(ArrayList<Integer> numProcessor, String vmURI) throws Exception ;
	
	
	/**
	 * This method remove ApplicationVM from the Coordinator
	 * 
	 * 
	 * @param vmURI
	 *            ApplicationVM URI
	 */
	public void removeVMfromCoordinator(String vmURI)throws Exception ; 
}