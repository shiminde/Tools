import gitlab
import sys
import time
import os
gl = gitlab.Gitlab('http://gitlab.com', private_token='nR5Diq')
gl.auth()
projects = gl.projects.list(all=True,order_by="last_activity_at")
os.remove("/opt/QA/sonar/ScannerRepoUrl.txt")
for project in projects:
        project_last_update_time = project.last_activity_at.split("T")[0]
        if project_last_update_time == time.strftime("%Y-%m-%d"):
                repourl = project.http_url_to_repo 
                group_name = repourl.split("/")[3]
                sub_group_name = repourl.split("/")[4]
		f = open('ScannerRepoUrl.txt','a')
                if group_name == "ETCYWPT" and sub_group_name != "doc":
			f.write(project.http_url_to_repo+"#master\n")
		elif sub_group_name == "doc" or group_name == "DATA":
		
			pass
		else:	
			f.write(project.http_url_to_repo+"#dev\n")
		#print project
f.close
