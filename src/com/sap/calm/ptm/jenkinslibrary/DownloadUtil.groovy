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
	 * @param jenkinsServer: server of the target jenkins
	 * @param jobName: name of the job which produced the target file
	 * @param date: which date's data do you want to collect?
	 * @param folderOfCurrentWorkspace: specify folder of current pipeline's workspace, the downloaded files will be putted here before archived
	 * @param relativePathOfToBeDownloadedFile: the target file's relative path in remote jenkins server, you can ignore the server part of the full path to prepare this parameter 
	 * @return: URI of the downloaded file
	 */
	public static List<String> downloadFromOtherJenkins(String jenkinsServer, String jobName, Date date, String folderOfCurrentWorkspace, String relativePathOfToBeDownloadedFile){
		JenkinsDownloader loader = new JenkinsDownloader(jenkinsServer,folderOfCurrentWorkspace,DOWNLOADSUBFOLDER)
		return loader.downloadFromJenkins(jobName, date , relativePathOfToBeDownloadedFile)
		
	}
	
	/**
	 * This method will format data which can be used for sending PDS notification later for the collected files.
	 * 
	 * @param project: what is the project does these files belongs to
	 * @param module: which module does these files belongs to
	 * @param category: e.g. ut_coverage_backend......
	 * @param fileFormat: e.g. ut_jacoco, eslint......
	 * @param files: the collected files' name
	 * @param urlPrefix: current jenkins' server + urlPrefix + file name = full path of the file in current jenkins server
	 * @return
	 */
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
	 * @param folder: 
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