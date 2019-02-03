package fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces;

import fr.sorbonne_u.components.interfaces.DataOfferedI;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.interfaces.TimeStampingI;

public interface AverageDynamicStateI extends DataOfferedI.DataI, DataRequiredI.DataI, TimeStampingI {

	
	/**
	 * Return ApplicationURI 
	 * 
	 */
	public String getApplicationURI();

	/**
	 * Return the Performance Average
	 * 
	 */
	public double getAveragePerApplication();

	/**
	 * Return the busiest ApplicationVM
	 * 
	 */
	public String getvmbusy();

	/**
	 * Return the freest ApplicationVM
	 * 
	 */
	public String getVMfree();

}
