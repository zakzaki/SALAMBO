package fr.sorbonne_u.datacenterclient.software.requestdispatcher.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.sorbonne_u.datacenter.ports.AbstractControlledDataOutboundPort;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDataConsumerI;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDynamicStateI;


public class AverageDynamicStateDataOutboundPort extends AbstractControlledDataOutboundPort
{
	private static final long	serialVersionUID = 1L ;
	protected String			ComponentURI ;

	
	public				AverageDynamicStateDataOutboundPort(
			ComponentI owner,
			String computerURI
			) throws Exception
		{
			super(owner) ;
			this.ComponentURI = computerURI ;

			assert	owner instanceof ComputerStateDataConsumerI ;
		}

		public				AverageDynamicStateDataOutboundPort(
			String uri,
			ComponentI owner,
			String computerURI
			) throws Exception
		{
			super(uri, owner);
			this.ComponentURI = computerURI ;

			assert	owner instanceof ComputerStateDataConsumerI ;
		}


		@Override
		public void			receive(DataRequiredI.DataI d)
		throws Exception
		{
			((AverageDataConsumerI)this.owner).
						acceptAverageData(this.ComponentURI,
													  (AverageDynamicStateI) d) ;
		}
	
	
}
