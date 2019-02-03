package fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces;


import fr.sorbonne_u.components.interfaces.DataOfferedI;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.interfaces.TimeStampingI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationInformation;


public interface RequestDispatcherDynamicStateI extends DataOfferedI.DataI, DataRequiredI.DataI, TimeStampingI {

	public ApplicationInformation getVMdetails() ;
	
}
