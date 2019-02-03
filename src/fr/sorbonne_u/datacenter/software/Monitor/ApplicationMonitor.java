package fr.sorbonne_u.datacenter.software.Monitor;


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
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.connectors.ControlledDataConnector;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataRequiredI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDataConsumerI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDynamicStateI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherDataConsumerI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.AverageDynamicStateDataOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports.RdDynamicStateDataOutboundPort;

/**
 * The class <code>ComputerMonitor</code> is a component used in the test to
 * act as a receiver for state data notifications coming from a computer.
 *
 * <p><strong>Description</strong></p>
 * 
 * The component class simply implements the necessary methods to process the
 * notifications without paying attention to do that in a really safe component
 * programming way. More or less quick and dirty...
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : April 24, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ApplicationMonitor
extends		AbstractComponent
implements	RequestDispatcherDataConsumerI,	AverageDataConsumerI
{
	// -------------------------------------------------------------------------
	// Constants and instance variables
	// -------------------------------------------------------------------------
	protected String ApplicationMonitorURI ; 
	protected Boolean					active ;
	protected String						ControllerAdmissionDynamicStateDataInboundPortURI ;
	protected String 						RequestDispatcherDynamicDataInboundPortURI ;
	protected RdDynamicStateDataOutboundPort		cdsPort ;
	protected AverageDynamicStateDataOutboundPort avgPort ; 

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				ApplicationMonitor(
		String ApplicationMonitroURI,
		String ControllerAdmission,
		String RequestDispatcher,
		Boolean active,
		String ControllerAdmissionDynamicStateDataInboundPortURI,
		String RequestDispatcherDynamicDataInboundPortURI 
		) throws Exception
	{
		super(ApplicationMonitroURI,1, 1) ;

		this.ApplicationMonitorURI=ApplicationMonitroURI;
		this.RequestDispatcherDynamicDataInboundPortURI = RequestDispatcherDynamicDataInboundPortURI;
		this.active = active ;
		this.ControllerAdmissionDynamicStateDataInboundPortURI =
				ControllerAdmissionDynamicStateDataInboundPortURI ;
		
		this.addOfferedInterface(DataRequiredI.PushI.class) ;
		this.addRequiredInterface(DataRequiredI.PullI.class) ;
	

		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class) ;
		this.cdsPort = new RdDynamicStateDataOutboundPort(this,
					ControllerAdmission) ;
		this.addPort(cdsPort) ;
		this.cdsPort.publishPort() ;
	
		
		
		
		this.avgPort = new AverageDynamicStateDataOutboundPort(this,
				RequestDispatcher) ;
		this.addPort(avgPort) ;
		this.avgPort.publishPort() ;
		this.tracer.setTitle("ApplicationMonitor");
		this.tracer.setRelativePosition(2, 0);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	@Override
	public void			start() throws ComponentStartException
	{
		
		super.start() ;

		// start the pushing of dynamic state information from the computer;
		// here only one push of information is planned after one second.
		try {
			this.doPortConnection(
					this.cdsPort.getPortURI(),
					this.ControllerAdmissionDynamicStateDataInboundPortURI,
					ControlledDataConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(
							"Unable to start the pushing of dynamic data from"
							+ " the comoter component.", e) ;
		}
		
	}

	@Override
	public void			execute() throws Exception
	{
		super.execute() ;
		
		this.cdsPort.startLimitedPushing(1000, 25) ;
		//this.avgPort.startLimitedPushing(1000, 10);
	}

	@Override
	public void			finalise() throws Exception
	{
		try {
			if (this.cdsPort.connected()) {
				this.cdsPort.doDisconnection() ;
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("port disconnection error", e) ;
		}
		super.finalise() ;
	}

	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.cdsPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException("port unpublishing error", e) ;
		}

		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------



	@Override
	public void		acceptDynamicData(
		String ControllerAdmission,
		RequestDispatcherDynamicStateI cds
		) throws Exception
	{
		
		if (this.active) {
			StringBuffer sb = new StringBuffer() ;
			
			sb.append("Accepting dynamic data from " + ControllerAdmission + "\n") ;
			sb.append("timestamp                : " +
											cds.getTimeStamp() + "\n") ;
			sb.append("timestamper id           : " +
											cds.getTimeStamperId() + "\n") ;

			sb.append("********** Informations **************** \n");
			
			
			for(int i=0 ; i<cds.getVMdetails().getVmURI().size();i++) {
				
				
			sb.append("--------------VMDetails---------------- \n");
			sb.append("Application "+ cds.getVMdetails().getVmURI().get(i).getApplicationUri()+" \n");
			sb.append("Allocated "+ cds.getVMdetails().getVmURI().get(i).getAllocatedcore()+ " \n");
			sb.append("appVM "+cds.getVMdetails().getVmURI().get(i).getUriVM() + " \n");
			sb.append("ComputerURI "+cds.getVMdetails().getVmURI().get(i).getComputerURI() + " \n");
			sb.append("      ---------CoresDetails---------------- \n");
			
			}
			this.logMessage(sb.toString()) ;
	}
	}
		

	@Override
	public void acceptStaticData(String computerURI, RequestDispatcherDynamicStateI staticState) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void acceptAverageData(String Component, AverageDynamicStateI currentDynamicState) throws Exception {
		if (this.active) {
			StringBuffer sb = new StringBuffer() ;
		
		if(currentDynamicState.getAveragePerApplication()!=0) {
			sb.append("**********************************AVERAGE****************************");
			sb.append("APPLICATION : "+currentDynamicState.getApplicationURI() + "\n" );
			sb.append("AVERAGE :  " + currentDynamicState.getAveragePerApplication() + "\n") ;
		}
		
		
		this.logMessage(sb.toString()) ;
	}

	}

}
