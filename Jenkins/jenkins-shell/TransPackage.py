#!/usr/bin/python
#coding=utf-8
import sys
import json
import subprocess
import os
import time
reload(sys)
sys.setdefaultencoding('utf8')
PROJECTNAME=sys.argv[1]


def RunShell(command):
        sub=subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
        sub.wait()
        Checklist=sub.stdout.read()
        #Checklist=json.loads(Checklist)
        return Checklist





ING="curl -s -X POST  -u 'admin:passwd' http://172.17.1.1:9000/api/ce/activity -d 'component=%s&status=IN_PROGRESS,PENDING' | grep %s" %(PROJECTNAME,PROJECTNAME)
RESON="1"
while ( RESON != ""):
	print "开始循环"
        time.sleep(10)
	RESON=RunShell(ING)

print "RESON:"+RESON
print "SONARQUBE扫描分析完成"
REPORTCOMMOND="curl -s -X POST  -u 'admin:passwd' http://172.17.1.1:9000/api/measures/component -d 'component=%s&metricKeys=alert_status'" %(PROJECTNAME)

VALUE=json.loads(RunShell(REPORTCOMMOND))
print VALUE
La=VALUE["component"]["measures"]
for L in La:
	if L["metric"] == "alert_status":
                print "质量阀状态:"+L["value"]
                if L["value"] != "OK":
			sys.exit(2)
			pass
