#!/usr/bin/python
#coding=utf-8
import os
import subprocess
import json
import sys
import numpy as np
import  pandas as pd
import smtplib  #加载smtplib模块
from email.mime.text import MIMEText
from email.utils import formataddr
import re
import datetime
import requests
reload(sys)
sys.setdefaultencoding('utf8')
now_time = datetime.datetime.now().strftime('%Y-%m-%d')
QAEMAIL=["shiminde@126.com","liuyujuan@126.com"]


f = open("/opt/QA/sonar/Recipient.txt","r")
EMAILADDRESS = {}
DINGTOKEN = {}
for i in f:
        EMAILADDRESS[i.strip().split(":")[0]]=i.strip().split(":")[1]
        DINGTOKEN[i.strip().split(":")[0]]=i.strip().split(":")[2]
		
PROJECTNAME=""
CHECKSONARCOMMOND="curl -s -X GET  -u 'admin:123' http://172.17.1.1:9000/api/ce/activity_status"
DIRCOMMON="ls CheckDir"
def RunShell(command):
        sub=subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
        sub.wait()
        Checklist=sub.stdout.read()
        #Checklist=json.loads(Checklist)
        return Checklist


def dd_robot(msg,token):
  HEADERS = {"Content-Type": "application/json;charset=utf-8"}
  url = "https://oapi.dingtalk.com/robot/send?access_token=%s"%(token)
  data_info = {
    "msgtype": "text",
    "text": {
    "content": msg
    },
    "isAtAll": True
  }
  value = json.dumps(data_info)
  response = requests.post(url,data=value,headers=HEADERS)


CHECKRESON = RunShell(CHECKSONARCOMMOND)
CHECKRESON = json.loads(CHECKRESON)
DIRRESON=RunShell(DIRCOMMON).splitlines()
if CHECKRESON["inProgress"] == 0:

                REPOURLCOMMOND="cat ScannerRepoUrl.txt  | awk -F '/' '{ print $4 }' | sort -r | uniq -c | awk '{ print $2 }'"
                P=RunShell(REPOURLCOMMOND)  #P=QA,SCM
                P=P.splitlines()

		SERVICE_DICT={}
		SERVICE_LIST=[]

		dic = dict()
		print P
                for PROJECTGROUP in P:
                        #print PROJECTGROUP
                        print "开始进行%s项目组" %(PROJECTGROUP)
                        CHECKCOMMOND="cat ScannerRepoUrl.txt | grep %s" %(PROJECTGROUP) #匹配出该项目组的url
                        CHECK_RESON=RunShell(CHECKCOMMOND)

                       #print CHECK_RESON #http://gitlab.com/PP/******.git#dev
                        i_NUM=0
                        #"%s"_SERVICE_LIST=[] %( P )
			print "#########################################"
                        for i in DIRRESON:   #循环CheckDir目录，获取目录名
				#print type(i)
                                if i in CHECK_RESON:    #如果该目录名包含在项目组的url内，相当于只执行本次项目组下的url
                                        SERVICE_LIST.append(i)					
					if not dic.get(PROJECTGROUP):
						dic[PROJECTGROUP]=list()
						dic[PROJECTGROUP].append(i)
						
					else:
						dic[PROJECTGROUP].append(i)
					
					print dic
					i_NUM=i_NUM+1
					#print "-------------------------"
					#print CHECK_RESON.splitlines()
					
					for F in CHECK_RESON.splitlines():
						#print "*****"
						Branch = F.split("#")[1]
						OO = F.split("/")[-1]
						OO = OO.split(".")[0]
						#print OO
						#print "*****"
						if i == OO:
							SERVICE_DICT["%s_branch" % i]=Branch


					#print "-------------------------"
					#SERVICE_DICT["%s_branch" % i]=L
                                        #print i
                                        #print i_NUM
                                        #print "本次扫描的服务是 %s" %(i) 



                                        PROJECTNAME = i+"-FULL"
                                        REPORTCOMMOND="curl -s -X POST  -u 'admin:123' http://172.17.1.1:9000/api/measures/component -d 'component=%s&metricKeys=ncloc,alert_status,bugs,new_bugs,vulnerabilities,new_vulnerabilities,code_smells,new_code_smells,coverage'" %(PROJECTNAME)

                                        #print PROJECTNAME
					VALUE=json.loads(RunShell(REPORTCOMMOND))
                                        #VALUE=RunShell(REPORTCOMMOND)
					#print VALUE["component"]["measures"]
					La=VALUE["component"]["measures"]
			                for L in La:
						#print L
						if L["metric"] == "quality_gate_details":
							SERVICE_DICT["%s_quality_gate_details" % i]=L["value"]
							#print  L["value"]
                                                if L["metric"] == "bugs":
                                                        #print "总BUG数:"+L["value"]
							SERVICE_DICT["%s_bugs" % i]=L["value"]
							#print SERVICE_DICT
                                                if L["metric"] == "new_bugs":
                                                        #print "新增BUG数:"+L["periods"][0]["value"]
							SERVICE_DICT["%s_new_bugs" % i]=L["periods"][0]["value"]
							pass
                                                if L["metric"] == "alert_status":
                                                        #print "质量阀状态:"+L["value"]
							SERVICE_DICT["%s_alert_status" % i]=L["value"]
                                                if L["metric"] == "vulnerabilities":
                                                        #print "漏洞:"+L["value"]
							SERVICE_DICT["%s_vulnerabilities" % i]=L["value"]
                                                if L["metric"] == "new_vulnerabilities":
							SERVICE_DICT["%s_new_vulnerabilities" % i]=L["periods"][0]["value"]
                                                if L["metric"] == "ncloc":
                                                        #print "行数:"+L["value"]
				 			SERVICE_DICT["%s_ncloc" % i]=L["value"]
                                                if L["metric"] == "coverage":
				 			SERVICE_DICT["%s_coverage" % i]=L["value"]
                                                if L["metric"] == "code_smells":
				 			SERVICE_DICT["%s_code_smells" % i]=L["value"]
                                                if L["metric"] == "new_code_smells":
							SERVICE_DICT["%s_new_code_smells" % i]=L["periods"][0]["value"]
					#print "start panduan-------"
					#print La
					if SERVICE_DICT.has_key("%s_new_code_smells" % i):
						pass
					else:
						SERVICE_DICT["%s_new_code_smells" % i]=0
						
						
					if SERVICE_DICT.has_key("%s_new_vulnerabilities" % i):
						pass
					else:
						SERVICE_DICT["%s_new_vulnerabilities" % i]=0
				

					if SERVICE_DICT.has_key("%s_new_bugs" % i):
						pass
					else:
						SERVICE_DICT["%s_new_bugs" % i]=0

					if SERVICE_DICT.has_key("%s_coverage" % i):
						pass
					else:
						SERVICE_DICT["%s_coverage" % i]=0
					if SERVICE_DICT.has_key("%s_ncloc" % i):
						pass
					else:
						SERVICE_DICT["%s_ncloc" % i]=0
					"""
					if "new_code_smells" not in La:
							print "new smells == 0"
							SERVICE_DICT["%s_new_code_smells" % i]=0
					if "new_vulnerabilities" not in La:
							print "new vulnerabilities == 0"
                                                        SERVICE_DICT["%s_new_vulnerabilities" % i]=0
					if "new_bugs" not in La:
							print "new bugs == 0"
                                                        SERVICE_DICT["%s_new_bugs" % i]=0
					if "coverage" not in La:
							print "new coverage == 0"
                                                        SERVICE_DICT["%s_coverage" % i]=0
					if "ncloc" not in La:
							print "new ncloc == 0"
                                                        SERVICE_DICT["%s_ncloc" % i]=0
					"""
					print "done panduan-------"

				#print SERVICE_LIST
				
else:
        print "Analysis Running"
#print dic
#print SERVICE_DICT
dic_list=dic.keys() #dic_list:项目组 list  
#print dic_list
for H in dic_list:
	#print dic[H]
#print SERVICE_LIST
#print SERVICE_DICT
	#print H
#Y=SERVICE_LIST
	#print dic
	Y=dic[H]
	df4=pd.DataFrame(
		 #columns=["service_name","alert_status","bug","vulnerabilities","ncloc"])
		 columns=["服务名","分支","质量阀状态","BUG","新增BUG","漏洞","新增漏洞","异味","新增异味","行数","覆盖率"])
	QQ=""
	
	for i in Y:
 		new=pd.DataFrame({'服务名':i,"分支":SERVICE_DICT["%s_branch" % i],"质量阀状态":SERVICE_DICT["%s_alert_status" % i],"BUG":SERVICE_DICT["%s_bugs" % i],"新增BUG":SERVICE_DICT["%s_new_bugs" % i],"漏洞":SERVICE_DICT["%s_vulnerabilities" % i],"新增漏洞":SERVICE_DICT["%s_new_vulnerabilities" % i],"异味":SERVICE_DICT["%s_code_smells" % i],"新增异味":SERVICE_DICT["%s_new_code_smells" % i],"行数":SERVICE_DICT["%s_ncloc" % i],"覆盖率":SERVICE_DICT["%s_coverage" % i]},index=[1])
		df4=df4.append(new,ignore_index=True,sort=False)
		QQ=QQ+"服务名:"+i+"    质量阀状态:"+SERVICE_DICT["%s_alert_status" % i]+"\n"+"详情: http://172.17.1.1:9000/dashboard?id=%s-FULL"%(i)+"\n"
	msg="扫描时间:%s\n"%(now_time)+QQ
	if DINGTOKEN.has_key(H):
		dd_robot(msg,DINGTOKEN[H])
	
	hangshu=df4.shape[0]
	NUMLIST=range(1,hangshu+1)
	df4.index = pd.Series(NUMLIST)
	html_head="""
<style type="text/css">
table.gridtable {
	font-family: verdana,arial,sans-serif;
	font-size:20px;
	color:#333333;
	border-width: 1px;
	border-color: #666666;
	border-collapse: collapse;
        text-align: center;
}
table.gridtable th {
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #666666;
	background-color: #dedede;
        text-align: center;
}
table.gridtable td {
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #666666;
	background-color: #ffffff;
        text-align: center;
}
</style>
		"""
	#print df4
	a=df4.to_html()
	a=html_head+a
	a=a.replace("<th></th>","<th>序号</th>")
	a=a.replace("dataframe","gridtable")
	a=a.replace("<td>ERROR</td>","<td><font color='red'>ERROR</font></td>")
	a=a.replace("<td>OK</td>","<td><font color='green'>OK</font></td>")
	a=a.replace("<th>质量阀状态</th>","<th nowrap='nowrap'>质量阀状态</th>")
	biaotoulist=["<th>序号</th>","<th>分支</th>","<th>服务名</th>","<th>质量阀状态</th>","<th>BUG</th>","<th>新增BUG</th>","<th>漏洞</th>","<th>新增漏洞</th>","<th>异味</th>","<th>新增异味</th>","<th>行数</th>","<th>覆盖率</th>"]
	
	for F in biaotoulist:
		L=F.replace("<th>","<th nowrap='nowrap'>")
		#print L
		a=a.replace(F,L)
		
	#print a
	HTML_TD=re.findall('<td>(.*)</td>',a)
	#print HTML_TD
	
	for i in Y:
		#print type(i)
		#print i
		a=a.replace("<td>%s</td>" %(i),"<td><a href=http://172.17.1.1:9000/dashboard?id=%s-FULL>%s</a></td>"%(i,i))
	"""
	for i in HTML_TD:
		if "OK" in i:
			HTML_TD.remove("OK")
		if "ERROR" in i:
			HTML_TD.remove("ERROR")
	for i in HTML_TD:
		if  i.isdigit():
			HTML_TD.remove(i)
		
	"""
	#print EMAILADDRESS[H]
	def mail():
		my_sender='scm@126.com' #发件人邮箱账号，为了后面易于维护，所以写成了变量
		if EMAILADDRESS.has_key(H):
			my_user=EMAILADDRESS[H].split(",") #收件人邮箱账号，为了后面易于维护，所以写成了变量

		else:
			my_user=QAEMAIL
		ret=True
		msg = MIMEText("""
				<center><font size="6">%s项目组服务扫描结果</center> <br/>
				<div style="font-size:24px" class=" width:100;overflow: hidden;">
					<span style="float:left;">点击服务名打开详细页面</span>
           				<span style="float:right;">%s</span>
       				</div> <br/>
				"""%(H,now_time)+a+"""
			<font size="4">质量阀规则：<a href="http://wiki.com/pages/viewpage.action?pageId=15315580"><font size="4">http://wiki.com/pages/viewpage.action?pageId=15315580</a>
						  """ 
			, 'html', 'utf-8')
		msg['From']=formataddr([my_sender,my_sender])   #括号里的对应发件人邮箱昵称、发件人邮箱账号
		#msg['To']=formataddr([my_user,my_user])   #括号里的对应收件人邮箱昵称、收件人邮箱账号
		msg['Subject']="%s_项目组服务扫描结果_%s" %(H,now_time)

		server=smtplib.SMTP_SSL("smtp.com",994)  #发件人邮箱中的SMTP服务器，端口是25
		server.login(my_sender,"passwd")    #括号中对应的是发件人邮箱账号、邮箱密码
		server.sendmail(my_sender,my_user,msg.as_string())   #括号中对应的是发件人邮箱账号、收件人邮箱账号、发送邮件
		#server.sendmail(my_sender,[my_user,],msg.as_string())   #括号中对应的是发件人邮箱账号、收件人邮箱账号、发送邮件
		server.quit()   #这句是关闭连接的意思

	ret=mail()
	print "done"
