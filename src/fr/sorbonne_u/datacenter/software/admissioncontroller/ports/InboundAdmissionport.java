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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestContlleurHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.RequestcontrolleurI;


/**
 * The class <code>InboundAdmissionport</code> implements the inbound port
 * offering the interface <code>InboundAdmissionport</code>.
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
 * invariant	uri != null and owner instanceof RequestSubmissionControllerHandlerI
 * </pre>
 * 
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class InboundAdmissionport extends AbstractInboundPort implements RequestSubmissionControllerI {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	owner instanceof RequestSubmissionControllerHandlerI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner
	 *            owner component.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	public InboundAdmissionport(ComponentI owner) throws Exception {
		super(RequestSubmissionControllerI.class, owner);

		assert owner instanceof RequestContlleurHandlerI;
	}

	/**
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	uri != null and owner instanceof RequestSubmissionHandlerI
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
	public InboundAdmissionport(String uri, ComponentI owner) throws Exception {
		super(uri, RequestSubmissionControllerI.class, owner);

		assert uri != null && owner instanceof RequestContlleurHandlerI;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequest(final RequestcontrolleurI r) throws Exception {
		this.getOwner().handleRequestAsync(new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {
				((RequestContlleurHandlerI) this.getOwner()).acceptRequestSubmission(r);
				return null;
			}
		});
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequestAndNotify(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequestAndNotify(final RequestcontrolleurI r) throws Exception {
		this.getOwner().handleRequestAsync(new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {

				((RequestContlleurHandlerI) this.getOwner()).acceptRequestSubmissionAndNotify(r);
				return null;
			}
		});

	}
}
