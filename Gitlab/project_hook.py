#!/usr/bin/python
#!coding=utf8
import gitlab
import time

class Git:
        def __init__(self):
                self.url = 'http://gitlabcom'
                self.private_token  = 'nR5Diq'
        def Auth(self):
                self.gl = gitlab.Gitlab(self.url, private_token=self.private_token)
	def Project_list(self):
		#输出了 项目名称，项目ID，项目URL，创建者ID
		self.projects = self.gl.projects.list(all=True)
		for project in self.projects:
        		print project.name,project.id,project.http_url_to_repo,project.creator_id
	def Project_create(self,group_name,project_name):
		#在组内创建分支
		groups = self.gl.groups.list(all=True)
		group_id=""
		for i in groups:
			if i.full_path == group_name:
				group_id = i.id
				project = self.gl.projects.create({'name': project_name, 'namespace_id': group_id},initialize_with_readme="true")
				
				branches = project.branches.list()
				branch = project.branches.create({'branch': 'dev',
                                  'ref': 'master'})
				branch.protect(developers_can_push=True, developers_can_merge=True)
				
				print "OK"
			else:
				print "No"
	def Project_hook(self):
		project = self.gl.projects.get('QA/eer')
		print project
		hooks = project.hooks.list()	
		for i in hooks:
			print i
if __name__=="__main__":
	obj = Git()
	obj.Auth()
	obj.Project_hook()
