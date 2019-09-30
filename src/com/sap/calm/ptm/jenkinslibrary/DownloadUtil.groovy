package com.sap.calm.ptm.jenkinslibrary

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.text.SimpleDateFormat
import java.nio.charset.StandardCharsets
import com.cloudbees.groovy.cps.NonCPS

import com.sap.calm.ptm.jenkinslibrary.JenkinsDownloader

class DownloadUtil{
	
	public static String DOWNLOADSUBFOLDER = 'pds_download_folder'
	
	/**
	 * Download test result from other jenkins server, this is static method which can be accessed from class, so it is easy for pipeline to comsume
	 * 
	 * @param jenkinsServer
	 * @param jobName
	 * @param date
	 * @param folderOfCurrentWorkspace
	 * @param relativePathOfToBeDownloadedFile
	 * @return: URI of the downloaded file
	 */
	public static List<String> downloadFromOtherJenkins(String jenkinsServer, String jobName, Date date, String folderOfCurrentWorkspace, String relativePathOfToBeDownloadedFile){
		JenkinsDownloader loader = new JenkinsDownloader(jenkinsServer,folderOfCurrentWorkspace,DOWNLOADSUBFOLDER)
		return loader.downloadFromJenkins(jobName, date , relativePathOfToBeDownloadedFile)
		
	}
	
	public static Map prepareDataForNotification(String project, String module, String category, String fileFormat, List<String> files, String urlPrefix){
		if(files == null || files.size() == 0) return null
		
		List fs = []
		for(int i = 0 ; i < files.size(); i++){
			Map file = [format: fileFormat, url: urlPrefix + '/' + files.get(i)]
			fs.add(file)
		}
		return [project: project, module: module, category:category, files: fs]
	}
	
	/**
	 * Sometimes we need to verify if a path or folder exist, make an utility
	 * @param path
	 * @return
	 */
	public static boolean fileOrFolderExists(String path){
		File file = new File(path)
		return file.exists()
	} 
	
	/**
	 * Delete the specified folder and all its sub objects
	 * 
	 * @param folder
	 * @return
	 */
	public static boolean clearTmpFolder(String parent){
		File folder = new File(parent)
		
		if(folder.exists() && folder.isDirectory()){
			File targetDir = new File(folder.absolutePath + File.separatorChar + DOWNLOADSUBFOLDER)
			
			if(targetDir.exists()){
				targetDir.deleteDir()
			}else{
				println "folder ${targetDir.path} doesn't exist"
			}
			return true
		}else{
			return false
		}
	}
}