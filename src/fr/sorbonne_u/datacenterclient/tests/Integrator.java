package fr.sorbonne_u.datacenterclient.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.sorbonne_u.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

/**
 * The class <code>Integrator</code> plays the role of an overall supervisor for
 * the data center example.
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
 * invariant		true
 * </pre>
 * 
 * <p>
 * Created on : 2018-09-21
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 * 
 */

public class Integrator extends AbstractComponent {
	protected String rmipURI;
	protected String csipURI;
	protected String avmipURI;
	protected String avmipURI1;
	protected String avmipURI2;
	/**
	 * Port connected to the request generator component to manage its execution
	 * (starting and stopping the request generation).
	 */
	protected RequestGeneratorManagementOutboundPort rmop;
	/** Port connected to the computer component to access its services. */
	protected ComputerServicesOutboundPort csop;
	/** Port connected to the AVM component to allocate it cores. */
	HashMap<String, ApplicationVMManagementOutboundPort> avmop;

	protected int numbercore;
	protected HashMap<String, Integer> AVMcores;
	protected String integratoruri;
	protected ArrayList<AllocatedCore[]> alloc;

	public Integrator(String integratoruri, HashMap<String, Integer> avmipURI, ArrayList<AllocatedCore[]> alloc)
			throws Exception {
		super(integratoruri, 0, 1);
		assert csipURI != null && avmipURI != null && rmipURI != null;
		avmop = new HashMap<String, ApplicationVMManagementOutboundPort>();
		for (Entry<String, Integer> entry : avmipURI.entrySet()) {
			avmop.put(entry.getKey(), new ApplicationVMManagementOutboundPort(this));
		}
		for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {
			this.addPort(entry.getValue());
			entry.getValue().publishPort();
		}
		this.alloc = alloc;
		this.integratoruri = integratoruri;
		this.addRequiredInterface(ApplicationVMManagementI.class);
		this.AVMcores = avmipURI;

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void start() throws ComponentStartException {

		try {

			for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {
				this.doPortConnection(entry.getValue().getPortURI(), entry.getKey(),
						ApplicationVMManagementConnector.class.getCanonicalName());

			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}

		try {
			this.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void execute() throws Exception {

		int i = 0;
		for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {

			entry.getValue().allocateCores(alloc.get(i));
			i++;
		}

		super.execute();

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {

		for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {
			this.doPortDisconnection(entry.getValue().getPortURI());
		}

		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		try {

			for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {
				entry.getValue().unpublishPort();
			}

		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void shutdownNow() throws ComponentShutdownException {
		try {

			for (Entry<String, ApplicationVMManagementOutboundPort> entry : avmop.entrySet()) {
				entry.getValue().unpublishPort();
			}

		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdownNow();
	}
}
