#!/usr/bin/python
#!coding=utf8
import gitlab
import time

class Git:
        def __init__(self):
                self.url = 'http://gitlab.com'
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
		groups = self.gl.groups.list()
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
			
	def Group_list(self):
		groups = self.gl.groups.list()
		for i in groups:
			print i.full_path,i.id
	def Search_group_id(self):
		groups = self.gl.groups.list()
		for i in groups:
	#		if i.full_path == "QA":
				#print i.full_path,i.id
			print i
	
	def Group_create(self,parentgroup_name,subgroup_name=""):
		groups = self.gl.groups.list()
		group_id=""
		
		exist=""
	
		for i in groups:
			if i.full_path == parentgroup_name:
				group_id = i.id
				print group_id
				exist="True"
		
		if exist=="True":
			group = self.gl.groups.create({'name': 'LLL', 'path': 'LLL'},parent_id=group_id)
			group = self.gl.groups.get("27")
			print group
		else:
			group = self.gl.groups.create({'name': 'LLL', 'path': 'LLL'},parent_id=group_id)
			
		
		#group.subgroups.create({'name': 'LLL','path': 'QA/LLL'})
			


	def User_list(self):
		users = self.gl.users.list()
		for i in users:
			print i
	def User_create(self):
		user = self.gl.users.create({'email': '984570656@qq.com',
                        'username': 'smd',
                        'name': 'smd'},reset_password='true')
		print "OK"
	
	def Tag_list(self):
		project = self.gl.projects.get('QA/multibranch-jenkins')
		tags = project.tags.list()
		print tags
	def Tag_create(self):
		project = self.gl.projects.get('QA/multibranch-jenkins')
		tag = project.tags.create({'tag_name': '3.0.0', 'ref': '6e80dbfb'}) #ref = 提交的HASH值 or 分支名 or tag 名
		tag.set_release_description('3.0.0')
def CreateProject():
	group_name = raw_input("请输入组名:")
	project_name = raw_input("请输入项目名:")
	obj.Project_create(group_name,project_name)

if __name__=="__main__":
	obj = Git()
	obj.Auth()
	#CreateProject()
	#obj.User_create()
	#obj.Group_create('group1','QQ')
	obj.Tag_create()
	obj.Tag_list()
