package fr.sorbonne_u.datacenter.software.controller.ports;

import java.util.ArrayList;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;
import fr.sorbonne_u.datacenter.software.controller.interfaces.RingnetworkI;

/**
 * The class <code>RingnetworkCoordinatorOutboundPort</code> implements the
 * inbound port offering the interface <code>RingnetworkI</code>.
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
 * invariant		owner instanceof RingnetworkI
 * </pre>
 * 
 * <p>
 * Created on : August 25, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */
public class RingnetworkCoordinatorOutboundPort extends AbstractOutboundPort implements RingnetworkI {
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public RingnetworkCoordinatorOutboundPort(ComponentI owner) throws Exception {
		super(RingnetworkI.class, owner);
	}

	public RingnetworkCoordinatorOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, RingnetworkI.class, owner);
	}

	@Override
	public void receiveVM(ArrayList<ApplicationVMPortsInformation> ringapplicationVM) throws Exception {
		((RingnetworkI) this.connector).receiveVM(ringapplicationVM);

	}
}