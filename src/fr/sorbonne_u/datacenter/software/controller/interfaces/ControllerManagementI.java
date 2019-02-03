package fr.sorbonne_u.datacenter.software.controller.interfaces;

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

/**
 * The interface <code>ControllerManagementI</code> defines the methods
 * to manage an application virtual machine component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : Jan, 2019</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public interface			ControllerManagementI
extends		OfferedI,
			RequiredI
{
	


	public void DestroyVM(String applicationURI,String appVM)throws Exception;
	
	/**
	 * addCores method tries to add a core number to the busiest ApplicationVM. The
	 * cores to be added must be on the same computer as the ApplicationVM. This
	 * searches if there are available resources and connect with the
	 * ApplicationVMManagement port to allocate resources to the ApplicationVM.
	 * 
	 * @throws Exception
	 */
	public void addCores() throws Exception;
	
	/**
	 * removeCores method tries to remove a core number from the freest
	 * ApplicationVM. If the number of core of an ApplicationVM exceeds 1, the
	 * method connects to the ApplicationVM Management to deallocate a number of
	 * cores. at the end it sends a notification to the PerformanceController
	 *
	 * @throws Exception
	 */
	public void removeCores()throws Exception ;
	
	/**
	 * The IncreaseFrequencyControl method increases the frequency of all
	 * ApplicationVMs in the application. the method retrieves the service ports of
	 * the computers to request the increase of the frequency. a notification is
	 * sent to the PerformanceController to inform it of the status of the frequency
	 * change
	 *
	 * @throws Exception
	 */
	
	public void IncreaseFrequencyControl() throws Exception;
	
	/**
	 * The DecreaseFrequencyControl method decreases the frequency of all
	 * ApplicationVMs in the application. the method retrieves the service ports of
	 * the computers to request the decrease of the frequency. a notification is
	 * sent to the PerformanceController to inform it of the status of the frequency
	 * change
	 *
	 * @throws Exception
	 */
	
	public void DecreaseFrequencyControl() throws Exception;
	
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
	
	public void CreateVM(String ApplicationURI, String requestDispatchermanagementuri, int numberVM) throws Exception;
	

}
