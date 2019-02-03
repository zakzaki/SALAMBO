package fr.sorbonne_u.datacenter.software.admissioncontroller.ports;

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
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerNotificationI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;


/**
 * The class <code>AdmissionControllerNotificationInboundPort</code> implements the
 * inbound port offering the interface <code>AdmissionControllerNotificationInboundPort</code>.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public class AdmissionControllerNotificationInboundPort extends AbstractInboundPort
		implements AdmissionControllerNotificationI {
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public AdmissionControllerNotificationInboundPort(ComponentI owner) throws Exception {
		super(AdmissionControllerNotificationI.class, owner);

		assert owner instanceof AdmissionControllerNotificationI;
	}

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	uri != null and owner instanceof ApplicationVMManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri
	 *            uri of the port.
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */
	public AdmissionControllerNotificationInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerNotificationI.class, owner);

		assert uri != null && owner instanceof AdmissionControllerNotificationI;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------


	@Override
	public void addcoresNotification(boolean findressouce) throws Exception {

		((AdmissionControllerNotificationI) this.getOwner()).addcoresNotification(findressouce);

	}

	@Override
	public void updateRingVM(boolean exist, ApplicationVMPortsInformation appinfo) throws Exception {
		((AdmissionControllerNotificationI) this.getOwner()).updateRingVM(exist, appinfo);

	}

	@Override
	public void frequencyIncreaseNotification(Boolean state) throws Exception {

		((AdmissionControllerNotificationI) this.getOwner()).frequencyIncreaseNotification(state);
	}

	@Override
	public void frequencyDecreaseNotification(Boolean state) throws Exception {
		((AdmissionControllerNotificationI) this.getOwner()).frequencyDecreaseNotification(state);
	}

	@Override
	public void removecoresNotification(boolean findressouce) throws Exception {
		((AdmissionControllerNotificationI) this.getOwner()).removecoresNotification(findressouce);
	}

	@Override
	public void addVMNotification(Boolean state) throws Exception {
		((AdmissionControllerNotificationI) this.getOwner()).addVMNotification(state);
		
	}

	@Override
	public void removeVMNotification(Boolean state) throws Exception {
	
		((AdmissionControllerNotificationI) this.getOwner()).removeVMNotification(state);
	}

}
