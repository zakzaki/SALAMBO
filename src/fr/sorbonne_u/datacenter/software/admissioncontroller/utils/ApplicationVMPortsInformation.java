package fr.sorbonne_u.datacenter.software.admissioncontroller.utils;

import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;

public class ApplicationVMPortsInformation {

	private String ApplicationVMURI  ; 
	private String SubmissionInboundPortURI ; 
	private String notificationOutboundPortURI ;
	private boolean freeVM ;
	private ApplicationVMinformation appVMinfo ; 
	
	
	public ApplicationVMPortsInformation(String ApplicationVMURI , String SubmissionInboundPortURI , String notificationOutboundPortURI , boolean freeVM , ApplicationVMinformation appVMinfo ) {
		
		this.ApplicationVMURI = ApplicationVMURI ;
		this.SubmissionInboundPortURI = SubmissionInboundPortURI; 
		this.notificationOutboundPortURI = notificationOutboundPortURI ; 
		this.freeVM = freeVM ; 
		this.appVMinfo = appVMinfo ; 
		
	}

	public ApplicationVMinformation getAppVMinfo() {
		return appVMinfo;
	}

	public void setAppVMinfo(ApplicationVMinformation appVMinfo) {
		this.appVMinfo = appVMinfo;
	}

	public String getApplicationVMURI() {
		return ApplicationVMURI;
	}
	public String getSubmissionInboundPortURI() {
		return SubmissionInboundPortURI;
	}

	public String getNotificationOutboundPortURI() {
		return notificationOutboundPortURI;
	}

	public boolean isFreeVM() {
		return freeVM;
	}

	public void setFreeVM(boolean freeVM) {
		this.freeVM = freeVM;
	}


	
	
}
