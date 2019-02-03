package fr.sorbonne_u.datacenter.software.admissioncontroller.utils;

import java.util.ArrayList;

import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;

public class ApplicationInformation {

	private ArrayList<ApplicationVMinformation> AppVM ; 


	public ArrayList<ApplicationVMinformation> getVmURI() {
		return AppVM;
	}
	public void setVmURI(ArrayList<ApplicationVMinformation> vmURI) {
		this.AppVM = vmURI;
	}
	public ApplicationInformation(ArrayList<ApplicationVMinformation> vmURI) {
		
		this.AppVM = vmURI ; 
	}
	
}
