package fr.sorbonne_u.datacenter.software.coordinator.ports;

import java.util.ArrayList;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.coordinator.interfaces.CoordinatorManagementI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>ApplicationVMManagementInboundPort</code> implements the
 * inbound port offering the interface <code>ApplicationVMManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		owner instanceof ApplicationVMManagementI
 * </pre>
 * 
 * <p>Created on : August 25, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CoordinatorServiceInboundPort
extends		AbstractInboundPort implements CoordinatorManagementI

{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	public				CoordinatorServiceInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(CoordinatorManagementI.class, owner) ;

		assert	owner instanceof CoordinatorManagementI ;
	}

	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			uri of the port.
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	public				CoordinatorServiceInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, CoordinatorManagementI.class, owner);

		assert	uri != null && owner instanceof CoordinatorManagementI ;
	}

	@Override
	public int ControlFrequency(int frequencycore, int processornum, int core, boolean state) throws Exception {
		return	((CoordinatorManagementI) this.getOwner()).ControlFrequency(frequencycore,processornum,core,state);
	}

	@Override
	public void UpdateCoordinator(ArrayList<Integer> numProcessor, String vmURI) throws Exception {
		
		((CoordinatorManagementI) this.getOwner()).UpdateCoordinator(numProcessor,vmURI);
		
	}

	@Override
	public void removeVMfromCoordinator(String vmURI) throws Exception {
		
		((CoordinatorManagementI) this.getOwner()).removeVMfromCoordinator(vmURI);
		
	}

	
}
