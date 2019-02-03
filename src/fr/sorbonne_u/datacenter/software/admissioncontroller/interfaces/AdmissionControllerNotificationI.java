package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

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

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;


/**
 * The interface <code>AdmissionControllerNotificationI</code> 
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * 
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public interface			AdmissionControllerNotificationI
extends		OfferedI,
			RequiredI
{
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
	public void frequencyIncreaseNotification(Boolean state) throws Exception;
	
	
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

	
	
	public void frequencyDecreaseNotification(Boolean state) throws Exception;
	
	
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
	
	public void addcoresNotification(boolean findressouce) throws Exception ;
	
	
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

	public void removecoresNotification(boolean findressouce) throws Exception ;
	
	
	
	
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
	
	public void addVMNotification(Boolean state) throws Exception ; 
	
	
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
	
	
	
	public void removeVMNotification(Boolean state) throws Exception ; 
	
	
	
	/**
	 * this method is used to add new ApplicationVM in the list of RingNetwork
	 * ApplicationVM . updateRingVM add the received from RequestDispatcher released
	 * ApplicationVM
	 * 
	 * @param appinfo
	 *            ApplicationVM port
	 */
	
	public void updateRingVM(boolean exist, ApplicationVMPortsInformation appinfo) throws Exception;  

}
