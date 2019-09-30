package com.sap.calm.ptm.jenkinslibrary

import com.sap.calm.ptm.jenkinslibrary.PDSNotificationBuilder
import com.sap.calm.ptm.jenkinslibrary.PDSNotification

class PDSNotificationUtil {
	public static boolean notifyPDS(
		String project, String module, String server, String serverType, String category, 
		String jobName, String buildId, Date whenTestRun, List files, String username, String pwd, boolean testMode){
		
		Logger log = new Logger()
		return notifyPDS(
			project, module, server, serverType, category,
			jobName, buildId, whenTestRun, files, username, pwd, testMode, log)
		
	}
	
	public static boolean notifyPDS(
		String project, String module, String server, String serverType, String category,
		String jobName, String buildId, Date whenTestRun, List files, String username, String pwd, boolean testMode,
		Logger logger){
		
		PDSNotificationBuilder notifBuilder = new PDSNotificationBuilder(
			project,
			module,
			server,
			serverType,
			category,
			username,
			pwd
		)
		notifBuilder.jobName(jobName).jobBuildId(buildId).whenTestRun(whenTestRun).testMode(testMode).logger(logger)
		
		if(files != null && !files.empty){
			for(int i = 0; i < files.size(); i++){
				//println files.get(i)
				notifBuilder.addFile(files.get(i).format,files.get(i).url);
			}
		}
		
		PDSNotification notification = notifBuilder.build()
		if(notification != null)
			return notification.send()
		else
			return false
	}
}
