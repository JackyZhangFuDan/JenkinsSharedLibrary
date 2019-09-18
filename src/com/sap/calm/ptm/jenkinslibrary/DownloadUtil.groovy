package com.sap.calm.ptm.jenkinslibrary

class DownloadUtil{
	String url = null
	String folder = null
	
	def DownloadUtil(String url,String folder){
		this.url = url
		this.folder = folder
		println('specified url:'+this.url)
		println('specified target folder:'+this.folder)
	}
	
	def boolean download(){
		return true
	}
}