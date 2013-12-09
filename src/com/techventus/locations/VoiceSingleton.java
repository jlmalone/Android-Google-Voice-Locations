package com.techventus.locations;

import java.io.IOException;

import com.techventus.server.voice.Voice;

/**
 * VoiceSingleton. Maintains a voice object in a singleton pattern for use throughout the application.
 * 
 */
public class VoiceSingleton {

	/** The singleton. */
	private static VoiceSingleton singleton;
	
	/** The Voice Object. */
	private static Voice voice;
	
	/**
	 * Instantiates a new voice singleton.
	 */
	private VoiceSingleton(){}
	
	/**
	 * Sets the voice.
	 *
	 * @param login the login
	 * @param password the password
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized void setVoice(String login, String password) throws IOException
	{

		singleton.voice = new Voice(login, password);

	}

	public static synchronized void reset()
	{
		try
		{
			singleton.voice = null;
		}
		catch (Exception e)
		{
		}
		try
		{
			singleton = null;
		}
		catch (Exception f)
		{
		}

	}


	/**
	 * Gets the or create voice singleton.
	 *
	 * @param login the login
	 * @param password the password
	 * @return the or create voice singleton
	 */
	public static synchronized VoiceSingleton getOrCreateVoiceSingleton(String login,String password) throws IOException{
		if(singleton==null){
			singleton = new VoiceSingleton();
			
			singleton.setVoice(login,password);

		}
		return singleton;
	}
	
	/**
	 * Gets the voice singleton.
	 *
	 * @return the voice singleton
	 */
	public static VoiceSingleton getVoiceSingleton(){
		if(singleton==null)
			singleton = new VoiceSingleton();
		return singleton;
	}
	
	
	/**
	 * Gets the Google Voice singleton object.
	 *
	 * @return the voice
	 * @throws Exception the exception
	 */
	public Voice getVoice() throws Exception{
		if(voice==null){
			throw new Exception("Voice Not Set");
		}
		return voice;
	}
}
