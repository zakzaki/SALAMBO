package fr.sorbonne_u.datacenterclient.software.requestdispatcher;


import fr.sorbonne_u.datacenter.data.AbstractTimeStampedData;
import fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces.AverageDynamicStateI;


public class RequestDispatcherDynamicState extends AbstractTimeStampedData implements AverageDynamicStateI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String ApplicationURI;
	protected double AveragePerApplication;
	protected String vmbusy;
	protected String vmfree; 

	public RequestDispatcherDynamicState(String ApplicationURI, double AveragePerApplication) throws Exception {
		super();
		this.AveragePerApplication = AveragePerApplication;
		this.ApplicationURI = ApplicationURI;
	}

	public RequestDispatcherDynamicState(String appURI, double average, String vMbusy) throws Exception {
		super();
		this.AveragePerApplication = average ; 
		this.ApplicationURI = appURI ; 
		this.vmbusy = vMbusy ; 
	}
	
	public RequestDispatcherDynamicState(String appURI, double average, String vMbusy,String vmfree) throws Exception {
		super();
		this.AveragePerApplication = average ; 
		this.ApplicationURI = appURI ; 
		this.vmbusy = vMbusy ; 
		this.vmfree = vmfree ;
	}

	public double getAveragePerApplication() {
		return AveragePerApplication;
	}

	public String getApplicationURI() {
		return ApplicationURI;
	}
	
	public String getvmbusy() {
		return vmbusy;
	}

	public String getVMfree() {
		return vmfree ; 
	}

}
