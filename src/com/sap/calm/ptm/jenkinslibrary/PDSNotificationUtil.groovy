package com.sap.calm.ptm.jenkinslibrary

import com.sap.calm.ptm.jenkinslibrary.PDSNotificationBuilder
import com.sap.calm.ptm.jenkinslibrary.PDSNotification

class PDSNotificationUtil {
	public static boolean notifyPDS(
		String project, String module, String server, String serverType, String category, 
		String jobName, String buildId, Date whenTestRun, List files, String username, String pwd){
		
		PDSNotificationBuilder notifBuilder = new PDSNotificationBuilder(
			project, 
			module, 
			server, 
			serverType, 
			category,
			username,  
			pwd
		)
		notifBuilder.jobName(jobName).jobBuildId(buildId).whenTestRun(whenTestRun)
		
		if(files != null && !files.empty){
			for(int i = 0; i < files.size(); i++){
				//println files.get(i)
				notifBuilder.addFile(files.get(i).format,files.get(i).url);
			}
		}
		
		PDSNotification notification = notifBuilder.build()
		return notification.send()
	}
}
