package com.sap.calm.ptm.jenkinslibrary

public class Logger{
	private List<String> messages = new ArrayList<String>()
	
	public add(String msg){
		this.messages.add(msg)
	}
	
	public allMsgs(){
		return this.messages
	} 
	
	public clear(){
		messages.clear()
	}
}