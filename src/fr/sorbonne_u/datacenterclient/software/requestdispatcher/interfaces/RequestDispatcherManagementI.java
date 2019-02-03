package fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;
import fr.sorbonne_u.datacenterclient.utils.ApplicationVMinformation;

public interface RequestDispatcherManagementI extends OfferedI, RequiredI {

	/**
	 * DisconnectVM disconnects an ApplicationVM from the application by
	 * disconnecting its submission port from the requestDispatcher port. the method
	 * keeps the connection with the notification port to ensure that the
	 * requestDispatcher has received all the notifications in order to fully
	 * release by sending the VM information to the AdmissionController to push it
	 * into the ring network
	 * 
	 * @param freeVM
	 *            The freest ApplicationVM
	 * 
	 */

	public void DisconnectVM(String URI) throws Exception;

	/**
	 * 
	 * this method provides the connection of a new ApplicationVM to the Application
	 * it ensures the connection of the submission ports as well as the notification
	 * ports using the reflection port. at the end it updates the structures that
	 * keep informations about the ApplicationVM of the Application
	 * 
	 * @param RequestSubmissionInboundportURI
	 *            ApplicationVM RequestSubmissionInboundPortURI
	 * @param vMURI
	 *            ApplicationVM URI
	 * @param vMnotificationoutboundporturi
	 *            ApplicationVM NotificationOutboundPort
	 * @param appinfo
	 *            ApplicationVM Information
	 * 
	 */

	public void ConnectVM(String RequestSubmissionInboundportURI, String vmURI, String outboundport,
			ApplicationVMinformation appVMinfo) throws Exception;

}
