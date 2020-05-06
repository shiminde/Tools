#!/usr/bin/python
#!coding=utf8
import gitlab
import time
import xlrd
import sys
import os
import subprocess

class Git:
        def __init__(self):
                self.url = 'http://gitlab.com'
                self.private_token  = 'nR5Di'
        def Auth(self):
                self.gl = gitlab.Gitlab(self.url, private_token=self.private_token)
	def Project_create(self):

		file=sys.argv[1]
		data = xlrd.open_workbook(file)
		table = data.sheets()[3]
		nrows = table.nrows
		cols = table.ncols
		for i in range(nrows):
                		value = table.row_values(i)
                		if "group" not in value and "project" not in value:
                        		print value

		#在组内创建分支
					groups = self.gl.groups.list(all=True)
					group_id=""
					for i in groups:
						if i.full_path == value[0]:
							group_id = i.id
							project = self.gl.projects.create({'name': value[1], 'namespace_id': group_id},initialize_with_readme="true")
				
							branches = project.branches.list()
							branch = project.branches.create({'branch': 'dev', 'ref': 'master'})
							branch.protect(developers_can_push=True, developers_can_merge=True)
				
							print "OK"
				else:
					print "No"
			
	def Create_Group(self,group_name):
		group_name = group_name
		self.group = self.gl.groups.create({'name':group_name,'path':group_name})	
		#print group.id
		Server = self.gl.groups.create({'name': 'Server', 'path': 'Server'},parent_id=self.group.id)
		Web = self.gl.groups.create({'name': 'Web', 'path': 'Web'},parent_id=self.group.id)
		Doc = self.gl.groups.create({'name': 'Doc', 'path': 'Doc'},parent_id=self.group.id)
		App = self.gl.groups.create({'name': 'App', 'path': 'App'},parent_id=self.group.id)
		Data = self.gl.groups.create({'name': 'Data', 'path': 'Data'},parent_id=self.group.id)	
		
		Docproject = self.gl.projects.create({'name': "doc", 'namespace_id': Doc.id},initialize_with_readme="true")
		branch = Docproject.branches.get('master')
		branch.protect(developers_can_push=True, developers_can_merge=True)
		
		Dataproject = self.gl.projects.create({'name': "data", 'namespace_id': Data.id},initialize_with_readme="true")
		Dataproject.share(334, gitlab.REPORTER_ACCESS)
		branch = Dataproject.branches.get('master')
		branch.protect(developers_can_push=True, developers_can_merge=True)



	def analysis_Excel(self):
		file=sys.argv[1]
		data = xlrd.open_workbook(file)
		table = data.sheets()[2]
		nrows = table.nrows
		devlist = []
		mainlist = []
		X=""
		for i in range(nrows):
			role = table.row_values(i)[0]
			#print role

			if role == u"Developer" or role == u"Maintainer":
				name = table.row_values(i)[2]
				if role == u"Developer":
					X = 0
					devlist.append(name)
				if role == u"Maintainer":
					X = 1
					mainlist.append(name)

			if role == "":
				name = table.row_values(i)[2]
				if X == 0:
					if name != "":
						devlist.append(name)
				if X == 1:
					if name != "":
						mainlist.append(name)

		#print devlist
		#print mainlist
		Nologin=[]
		devlist = list(set(devlist).difference(set(mainlist)))
		for dev in devlist:
			print dev
			user = self.gl.users.list(username=dev)
			if len(user):
				user_id = user[0].id
				member = self.group.members.create({'user_id': user_id,'access_level': gitlab.DEVELOPER_ACCESS})
			else:
				Nologin.append(dev)
		for main in mainlist:
			print main
			user = self.gl.users.list(username=main)
			if len(user):
				user_id = user[0].id
				member = self.group.members.create({'user_id': user_id,'access_level': gitlab.MAINTAINER_ACCESS})
			else: 
				Nologin.append(dev)
if __name__=="__main__":
	obj = Git()
	obj.Auth()
	#obj.Create_Group("Test123")
	#obj.analysis_Excel()
	obj.Project_create()
