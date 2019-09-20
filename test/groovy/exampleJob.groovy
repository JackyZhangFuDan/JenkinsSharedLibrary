import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil

def execute() {
    node() {
        stage("First") {
            echo "Something"
        }
        stage("Middle") {
            DownloadUtil util = new DownloadUtil()
			def log = ""
            println util.downloadFromJenkins('https://gkecalmdevshanghai.jaas-gcp.cloud.sap.corp', 'PTM_CN_FRAME_UI',new Date(1568611787631),pwd(),'artifact/target/frame/UT/coverage/IE%2011.0.0%20(Windows%2010.0.0)/node_modules/karma-qunit/lib/index.html')
			println log;
        }
		
        stage("Last") {
            echo "We're done"
        }
    }
	
}

def pwd(){
	return 'C:\\Users\\i042102\\Downloads'
}

return this