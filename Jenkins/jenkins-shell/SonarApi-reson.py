#!/usr/bin/python
#coding=utf-8
import sys
import json
import subprocess
import os
reload(sys)
sys.setdefaultencoding('utf8')

SYSREMNAME=sys.argv[1]
def RunShell(command):
        sub=subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
        sub.wait()
        Checklist=sub.stdout.read()
        #Checklist=json.loads(Checklist)
        return Checklist

PROJECTNAME = SYSREMNAME
REPORTCOMMOND="curl -s -X POST  -u 'admin:123' http://172.17.1.1:9000/api/measures/component -d 'component=%s&metricKeys=ncloc,alert_status,bugs,vulnerabilities,code_smells,files,lines,duplicated_lines_density,comment_lines_density'" %(PROJECTNAME)
STRVALUE=RunShell(REPORTCOMMOND)
VALUE=json.loads(RunShell(REPORTCOMMOND))
#print VALUE
La=VALUE["component"]["measures"]
for L in La:
	if L["metric"] == "bugs":
		print "BUG数:"+L["value"]
	if L["metric"] == "vulnerabilities":
		print "漏洞数:"+L["value"]
	if L["metric"] == "alert_status":
		print "质量阀状态:"+L["value"]
		if L["value"] == "OK":
			GITHASH=os.popen("git rev-list HEAD -n 1")
			GITHASH=GITHASH.readlines()
			GITHASH = [x.strip() for x in GITHASH if x.strip() != '']
			GITHASH = ''.join(GITHASH)
			GITHASH8 = GITHASH[0:8]
			SONARPATH="/jenkinsdata/data/.jenkins/common-extended/%s-HASH.txt" %(PROJECTNAME)
			f = open(SONARPATH,'w') 
			f.write(GITHASH8)
			f.close()
	if L["metric"] == "code_smells":
		print "代码异味:"+L["value"]
	if L["metric"] == "lines":
		print "代码行:"+L["value"]
	if L["metric"] == "files":
		print "文件数:"+L["value"]
	if L["metric"] == "duplicated_lines_density":
		print "重复率:"+L["value"]+"%"
	if L["metric"] == "comment_lines_density":
		print "注释率:"+L["value"]+"%"

HLIST=["files","lines","duplicated_lines_density","comment_lines_density","code_smells","vulnerabilities","bugs","alert_status"]
for I in HLIST:
	if I not in STRVALUE:
		if I == "files":
			print "文件数:0"
		if I == "lines":
			print "代码行:0"
		if I == "duplicated_lines_density":
			print "重复率:0%"
		if I == "bugs":
			print "BUG数:0"
		if I == "vulnerabilities":
			print "漏洞数:0"
		if I == "code_smells":
			print "代码异味:0"
		if I == "alert_status":
			print "质量阀状态:OK"
		if I == "comment_lines_density":
			print "注释率:0%"

#如果质量阀不OK的话,将构建置为失败
for L in La:
	if L["metric"] == "alert_status":
		if L["value"] != "OK":
			sys.exit(1)
