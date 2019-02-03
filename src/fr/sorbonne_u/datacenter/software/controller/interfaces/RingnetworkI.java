package fr.sorbonne_u.datacenter.software.controller.interfaces;

import java.util.ArrayList;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.utils.ApplicationVMPortsInformation;

public interface RingnetworkI extends		OfferedI,
RequiredI {

	void receiveVM(ArrayList<ApplicationVMPortsInformation> ringapplicationVM) throws Exception; 
}
