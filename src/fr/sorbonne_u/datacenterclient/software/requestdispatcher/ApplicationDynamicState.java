package fr.sorbonne_u.datacenterclient.software.requestdispatcher;

import fr.sorbonne_u.datacenter.data.AbstractTimeStampedData;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationInformation;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;


public class ApplicationDynamicState extends AbstractTimeStampedData implements RequestDispatcherDynamicStateI{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ApplicationInformation  VMdetails ;
	
	
	public				ApplicationDynamicState(
			ApplicationInformation appVM 
			) throws Exception
		{
			super() ;

			assert appVM != null ;

		
			this.VMdetails = appVM ; 
		}
	
	

	
	public ApplicationInformation  getVMdetails() {
		return VMdetails;
	}

	
	
	
}
