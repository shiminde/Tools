#!/usr/bin/python
#coding:utf-8
#pip install dingtalkchatbot
from urllib2 import Request, urlopen, URLError, HTTPError
import json
from dingtalkchatbot.chatbot import DingtalkChatbot
import ssl
import time
import jenkins
ssl._create_default_https_context = ssl._create_unverified_context

WEBURL=["http://harborxa.com","http://harborbj.com"]



webhook="https://oapi.dingtalk.com/robot/send?access_token=a1ce58d3495e8abccf051a016743c94f627"



def DingMessage(url,reason):
    print reason
    Ding = DingtalkChatbot(webhook)
    Ding.send_text(msg=str(reason), is_at_all=True)
    print "Ok"
    #Ding.send_text(msg=str(reason))


def show_status(url):
    req = Request(url)
    try:
         response = urlopen(req,timeout=10.0)
    except HTTPError, e:
         reason=url,",'Error code: ',",e.code
	 if e.code != 403 and e.code != 401:
	 	print url,'code: ', e.code
         	DingMessage(url,reason)
         
    except URLError, e:
         reason=url,",'Error Reason: ',",e.reason
         DingMessage(url,reason)
         print url,'Reason: ', e.reason
  #  else:
  #      print 'Success'
def check_jenkins_node():
	offlines_dict = {}
	
	ciurl = ["http://jenkinsbj.com:8080","http://jenkinsxa.com:8080"]
	for url in ciurl:
		offlines_list = []
		try:
			print url
			server = jenkins.Jenkins(url, username='jenkins', password='123456')
		except Exception as e:
			print e.reason
			
		else:
			for i in server.get_nodes():
				#print i
				if i["offline"] == True:
					offlines_list.append(i['name'])
			#print offlines_list
		if offlines_list != []:
			offlines_dict[url]=offlines_list
	#print offlines_dict
	for i in offlines_dict:
		#print i,offlines_dict[i]
		message="Error,Jenkins地址:%s,节点%s不在线"%(i,offlines_dict[i])
		DingMessage(i,message)


def main():
    print time.strftime('%Y-%m-%d %H:%M:%S')
    for url in WEBURL:
	print url
        show_status(url)
if __name__=="__main__":
    main()
    check_jenkins_node()
