#!/usr/bin/python
#!coding=utf-8
import jenkins
import sys
import time
import os 
server = jenkins.Jenkins('http://192.168.1.1:8080/jenkins/', username='shiminde', password='passwd')
#print server.get_jobs()[0]['jobs'][0]['url']
#print server.get_jobs()[0]
#print server.jobs_count()
a=0
url_file = "/jenkinsdata/data/.jenkins/common-extended"

job_name = sys.argv[1]

job_basename = job_name.split('/')[1]

print job_basename

print "开始构建: %s "  %(job_name)



#for l in server.get_jobs():
	#print l['jobs']
#	print l['name']
#	for i in l['jobs']:                  #输出所有的job列表
#		print i['url']
#		a=a+1
	
next_build_number = server.get_job_info(job_name)['nextBuildNumber']

print "构建号: %s " %(next_build_number)

server.build_job(job_name)

from time import sleep; sleep(10)

lastbuildnum = server.get_job_info(job_name)['lastBuild']['number']


build_ing=server.get_build_info(job_name,next_build_number)['building']   #构建是否进行中

while build_ing == True:
	print "---构建进行中----"
	print time.strftime("%Y-%m-%d %H:%M:%S")
	from time import sleep; sleep(10)
	build_ing=server.get_build_info(job_name,next_build_number)['building']   #构建是否进行中
	
print "构建完毕"
print time.strftime("%Y-%m-%d %H:%M:%S")

build_result=server.get_build_info(job_name,next_build_number)['result']     #构建结果

print "job_status: %s" %(build_result)

url=os.popen("ssh 192.168.1.1 cat {}/{}.txt".format(url_file,job_basename)).readlines()
package_url=[]
for i in url:
	#print i
 	q=i.replace('\n','')
	package_url.append(q)

print "构建包地址: %s" %(package_url)

#print "构建生成包的地址为:%s" %(url)
#print lastresult
#print a
