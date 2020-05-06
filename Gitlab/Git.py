import gitlab
import sys
import time
import os
gl = gitlab.Gitlab('http://gitlab.com', private_token='nR5Diq5')
gl.auth()
projects = gl.projects.list(all=True,order_by="last_activity_at")
for project in projects:
	project_last_update_time = project.last_activity_at.split("T")[0]
	if project_last_update_time == time.strftime("%Y-%m-%d"):
		#print project.http_url_to_repo 
		f = open('GitUrl.txt','a')
		f.write(project.http_url_to_repo+"\n")

f.close
os.system("sed -i /data.git/d GitUrl.txt  && sed -i s/$/#dev/g GitUrl.txt")
#project = gl.projects.list(search="Smdtest")
#project = gl.projects.get(340)
#print projecta
#project.protectedbranches.delete('dev')
#p_branches = project.branches.list()


# gitlab.MAINTAINER_ACCESS,gitlab.DEVELOPER_ACCESS

#p_branch = project.protectedbranches.create({
#    'name': 'dev',
#    'merge_access_level': gitlab.DEVELOPER_ACCESS,
#    'push_access_level': gitlab.DEVELOPER_ACCESS
#})

#dev.protect(developers_can_push=True, developers_can_merge=True)

#for i in  p_branches:
#	print i
#users = gl.users.list(all=True)
#users = gl.users.list(username='shiminde')
#print users[0].id
