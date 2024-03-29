package fr.sorbonne_u.datacenter.hardware.computers.interfaces;

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
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;

/**
 * The interface <code>ComputerServicesI</code> defines the services offered by
 * <code>Computer</code> components (allocating cores).
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: add the deallocation of cores.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : April 9, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			ComputerServicesI
extends		OfferedI,
			RequiredI
{
	/**
	 * allocate one core on this computer and return an instance of
	 * <code>AllocatedCore</code> containing the processor number,
	 * the core number and a map giving the URI of the processor
	 * inbound ports; return null if no core is available.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return				an instance of <code>AllocatedCore</code> with the data about the allocated core.
	 * @throws Exception		<i>todo.</i>
	 */
	public AllocatedCore	allocateCore() throws Exception ;

	/**
	 * allocate up to <code>numberRequested</code> cores on this computer and
	 * return and array of <code>AllocatedCore</code> containing the data for
	 * each requested core; return an empty array if no core is available.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	numberRequested &gt; 0
	 * post	return.length &gt;= 0 and return.length &lt;= numberRequested
	 * </pre>
	 *
	 * @param numberRequested	number of cores to be allocated.
	 * @return					an array of instances of <code>AllocatedCore</code> with the data about the allocated cores.
	 * @throws Exception			<i>todo.</i>
	 */
	public AllocatedCore[]	allocateCores(final int numberRequested)
	throws Exception ;
	
	
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
	
	
	public void DeallocateCores(AllocatedCore[] vmcores , String vmURI) throws Exception;
	
	
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

	
	public boolean IncreaseFrequency(AllocatedCore[] allocatedcore) throws Exception;
	
	/**
	 * 
	 * this method is used to increase the frequency of the cores. allocatedCore[]
	 * is used to know the processor and the cores that request the increase of the
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

	
	public boolean DecreaseFrequency(AllocatedCore[] allocatedcore) throws Exception ;
	
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
	
	public int getAvailableCoresnumber()throws Exception;
	
	
	/**
	 * 
	 * updateCoordinator update the Coordinator list of ApplicationVM , the
	 * Coordinator have information about ApplicationVM which shares processors of
	 * the computer to coordinate between Applications
	 * 
	 * @param allocatedCores
	 *            AllocatedCore[] in order to know the cores for frequency increase
	 * @param uriVM
	 *            ApplicationVM URI
	 * @throws Exception
	 */
	
	public void updateCoordinator(AllocatedCore[] allocatedCores, String uriVM) throws Exception;
}
