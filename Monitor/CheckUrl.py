#!/usr/bin/python
#coding:utf-8  
#pip install dingtalkchatbot
from urllib2 import Request, urlopen, URLError, HTTPError  
import json
from dingtalkchatbot.chatbot import DingtalkChatbot

WEBURL=["http://b.com","http://a.com"]

webhook="https://oapi.dingtalk.com/robot/send?access_token=0d68b88805c9f6105"

def DingMessage(url,reason):
    Ding = DingtalkChatbot(webhook)
    Ding.send_text(msg=str(reason), is_at_all=True)


def show_status(url):     
    req = Request(url)
    try:  
         response = urlopen(req)  
    except HTTPError, e:  
         reason=url,",'Error code: ',",e.code
	 DingMessage(url,reason)
    except URLError, e:  
         reason=url,",'Error Reason: ',",e.reason
	 DingMessage(url,reason)
         print url,'Reason: ', e.reason  
  #  else:  
  #      print 'Success'  
  
def main(): 
    for url in WEBURL:
    	show_status(url)  
if __name__=="__main__":  
    main()  
