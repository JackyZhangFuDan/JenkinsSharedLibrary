package com.sap.calm.ptm.jenkinslibrary

import com.cloudbees.groovy.cps.NonCPS

public class Logger{
	private List<String> messages = new ArrayList<String>()
	
	@NonCPS
	public add(String msg){
		if(msg != null && !msg.isEmpty())
			this.messages.add(msg)
		else
			this.messages.add('<empty msg>')
	}
	@NonCPS
	public allMsgs(){
		return this.messages
	} 
	@NonCPS
	public clear(){
		messages.clear()
	}
}