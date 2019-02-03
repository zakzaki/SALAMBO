package fr.sorbonne_u.datacenterclient.utils;

import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;

public class ApplicationVMinformation {

	private String ApplicationUri ; 
	private AllocatedCore[] allocatedcore ;
	private String ComputerURI ;
	private String uriVM ; 
	private String computerserviceuri ; 
	private String VMManagementuri ; 
	
	
	
	public				ApplicationVMinformation(
			
			String uriVm,
			AllocatedCore[] allocatedcore,
			String ComputerURI,
			String ApplicationURI,
			String computerserviceuri,
			String VMManagementuri
			) throws Exception
		{
			super() ;

			assert	uriVm != null && allocatedcore != null && ComputerURI!=null ; 

			this.uriVM = uriVm ; 
			this.ApplicationUri=ApplicationURI;
			this.allocatedcore = allocatedcore ; 
			this.ComputerURI = ComputerURI ; 
			this.computerserviceuri = computerserviceuri ; 
			this.VMManagementuri = VMManagementuri ; 
		}
	
	

	public				ApplicationVMinformation() throws Exception
		{
			super() ;
			
		}

	
	
	public String getVMManagementuri() {
		return VMManagementuri;
	}




	public void setVMManagementuri(String vMManagementuri) {
		VMManagementuri = vMManagementuri;
	}




	public String getComputerserviceuri() {
		return computerserviceuri;
	}




	public void setComputerserviceuri(String computerserviceuri) {
		this.computerserviceuri = computerserviceuri;
	}




	public String getUriVM() {
		return uriVM;
	}




	public void setUriVM(String uriVM) {
		this.uriVM = uriVM;
	}




	public String getApplicationUri() {
		return ApplicationUri;
	}




	public void setApplicationUri(String applicationUri) {
		ApplicationUri = applicationUri;
	}

	public AllocatedCore[] getAllocatedcore() {
		return allocatedcore;
	}
	public void setAllocatedcore(AllocatedCore[] allocatedcore) {
		this.allocatedcore = allocatedcore;
	}
	public String getComputerURI() {
		return ComputerURI;
	}
	public void setComputerURI(String computerURI) {
		ComputerURI = computerURI;
	}
	

}
