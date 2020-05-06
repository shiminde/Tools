#!/usr/bin/python
#coding=utf-8
import smtplib  #加载smtplib模块
import os
from email.mime.text import MIMEText
from email.utils import formataddr
import sys
import datetime

reload(sys)
sys.setdefaultencoding('utf8')


PROJECT=sys.argv[1]              #项目名
SONARPATH=sys.argv[2]            #SONAR结果路径
BUILDLOGURL=sys.argv[3]          #构建LOG地址
BUILDSTATUS=sys.argv[4]          #构建状态 OK、ERROR
BRANCH=sys.argv[5]           #邮件接收人
RECIPIENTS=sys.argv[6]           #邮件接收人
#print SONARPATH
#print PROJECT
#print RECIPIENTS

print "打印分支"
print BRANCH
if "*" in BRANCH:
	BRANCH=BRANCH.split("/")[1]
	
print BRANCH
RECIPIENTS=RECIPIENTS.split(",") #邮件接收人


if "MULTI" in PROJECT:
	print "多分支情况"
	SERVICENAME=PROJECT.split("_")[2]
	SERVICENAME=SERVICENAME+"_"+BRANCH
else:
	SERVICENAME=PROJECT.split("_")[2]
SONARQUEBURL="http://172.17.1.1:9000/dashboard?id=%s"%SERVICENAME
RESON={}
file = open(SONARPATH)
for line in file:
    line = line.replace("\n","")
    line = line.split(":",1)
    RESON[line[0]]=line[1]

file.close()

starttime=RESON["starttime"].split(".")[0]
endtime=RESON["endtime"].split(".")[0]
starttime=datetime.datetime.strptime(starttime, "%Y-%m-%d %H:%M:%S")
endtime=datetime.datetime.strptime(endtime, "%Y-%m-%d %H:%M:%S")
print starttime
print endtime

TIMEALL=(endtime-starttime).seconds
TIMEALL=datetime.timedelta(seconds=TIMEALL)
print TIMEALL

my_sender='scm@sinoiov.com' #发件人邮箱账号，为了后面易于维护，所以写成了变量
my_user=["shiminde@sinoiov.com","liuyujuan@sinoiov.com"] #收件人邮箱账号，为了后面易于维护，所以写成了变量

a="""
<center><font size="6">%s项目组服务扫描结果</center> <br/>
<font size="5">服务构建报告<br/>
             <font size="4">构建名称：%s<br/>
             <font size="4">构建分支：%s<br/>
             <font size="4">构建时间：%s<br/>
             <font size="4">构建状态：%s<br/>
             <font size="4">构建日志地址：<a href=%s>%s</a><br/>
	     <br/><hr/>
<font size="5">服务扫描报告<br/>

             <font size="4">质量阀状态：%s&nbsp;&nbsp;&nbsp;&nbsp;(质量阀定义：BUG 大于 0个 ；漏洞 大于 0个；异味 大于 30个)<br/>
             <font size="4">代码行：%s行<br/>
             <font size="4">文件数：%s个<br/>
             <font size="4">BUG: %s个<br>
             <font size="4">漏洞：%s个<br/>
             <font size="4">代码异味：%s个<br/>
             <font size="4">重复率：%s<br/>
             <font size="4">注释率：%s<br/>
             <font size="4">扫描报告详见：<a href=%s>%s</a><br/>
  


"""%(SERVICENAME,PROJECT,BRANCH,TIMEALL,BUILDSTATUS,BUILDLOGURL,BUILDLOGURL,RESON["质量阀状态"],RESON["代码行"],RESON["文件数"],RESON["BUG数"],RESON["漏洞数"],RESON["代码异味"],RESON["重复率"],RESON["注释率"],SONARQUEBURL,SONARQUEBURL)


a=a.replace("OK","<font color='green'>OK</font>")
a=a.replace("ERROR","<font color='red'>ERROR</font>")

	



def mail():
    ret=True
    msg = MIMEText(a, 'html', 'utf-8')
    msg['From']=formataddr([my_sender,my_sender])   #括号里的对应发件人邮箱昵称、发件人邮箱账号
    #msg['To']=formataddr([my_user,my_user])   #括号里的对应收件人邮箱昵称、收件人邮箱账号
    msg['Subject']="%s_项目组服务扫描结果" %SERVICENAME

    server=smtplib.SMTP_SSL("smtp.XXXX.com",994)  #发件人邮箱中的SMTP服务器，端口是25
    server.login(my_sender,"PASSWD")    #括号中对应的是发件人邮箱账号、邮箱密码
    #server.sendmail(my_sender,my_user,msg.as_string())   #括号中对应的是发件人邮箱账号、收件人邮箱账号、发送邮件
    server.sendmail(my_sender,RECIPIENTS,msg.as_string())   #括号中对应的是发件人邮箱账号、收件人邮箱账号、发送邮件
    server.quit()   #这句是关闭连接的意思

if RESON["质量阀状态"] == "ERROR":
	ret=mail()
