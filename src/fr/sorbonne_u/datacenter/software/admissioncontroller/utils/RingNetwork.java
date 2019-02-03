package fr.sorbonne_u.datacenter.software.admissioncontroller.utils;

public class RingNetwork {

	private String LastOutboundportURI ; 
	private String ControllerAdmissionRingNetworkinboundportURI ;
	private String LastControllerURI ; 
	
	public String getLastOutboundportURI() {
		return LastOutboundportURI;
	}
	public String getLastControllerURI() {
		return LastControllerURI;
	}
	public void setLastControllerURI(String lastControllerURI) {
		LastControllerURI = lastControllerURI;
	}
	public void setLastOutboundportURI(String lastOutboundportURI) {
		LastOutboundportURI = lastOutboundportURI;
	}
	public String getControllerAdmissionRingNetworkinboundportURI() {
		return ControllerAdmissionRingNetworkinboundportURI;
	}
	public void setControllerAdmissionRingNetworkinboundportURI(String controllerAdmissionRingNetworkinboundportURI) {
		ControllerAdmissionRingNetworkinboundportURI = controllerAdmissionRingNetworkinboundportURI;
	}
	
	
	
	
}
