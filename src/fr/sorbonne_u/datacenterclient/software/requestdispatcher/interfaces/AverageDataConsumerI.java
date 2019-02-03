package fr.sorbonne_u.datacenterclient.software.requestdispatcher.interfaces;

public interface			AverageDataConsumerI
{
	/**
	 * accept the static data pushed by a computer with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	computerURI != null and staticState != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param computerURI	URI of the computer sending the data.
	 * @param staticState	static state of this computer.
	 * @throws Exception		<i>todo.</i>
	 */
	
	/**
	 * accept the dynamic data pushed by a computer with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	computerURI != null and currentDynamicState != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param computerURI			URI of the computer sending the data.
	 * @param currentDynamicState	current dynamic state of this computer.
	 * @throws Exception				<i>todo.</i>
	 */
	public void			acceptAverageData(
		String					ComponentURI,
		AverageDynamicStateI	currentDynamicState
		) throws Exception ;
}