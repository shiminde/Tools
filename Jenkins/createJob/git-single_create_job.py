#!/usr/bin/python
#!coding=utf-8
import jenkins
import sys
import time
import os
import xlrd

file=sys.argv[1]

data = xlrd.open_workbook(file)
table = data.sheets()[0]
nrows = table.nrows

server = jenkins.Jenkins('http://192.168.1.1:8080/jenkins/', username='shiminde', password='passwd')


my_job = server.get_job_config('QA/GIT-SINGLE-MAVEN-EXAMPLE')
##print my_job


for i in range(nrows):
 	if i != 0:
		a = table.row_values(i)
		print a[0],a[1],a[2],a[3],a[4]
		JOB_CONFIG=my_job.replace('SVNURL',a[1])\
				.replace('BRANCH',a[2])\
				.replace('sparseDIR',a[3])\
		 		.replace('JDK',a[4])\
				.replace('PROFILE',a[5])

		server.create_job(a[0],JOB_CONFIG)

