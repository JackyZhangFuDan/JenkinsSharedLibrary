package com.sap.calm.ptm.jenkinslibrary

public class Logger{
	private List<String> messages = new ArrayList<String>()
	
	public add(String msg){
		if(msg != null && !msg.isEmpty())
			this.messages.add(msg)
		else
			this.messages.add('<empty msg>')
	}
	
	public allMsgs(){
		return this.messages
	} 
	
	public clear(){
		messages.clear()
	}
}