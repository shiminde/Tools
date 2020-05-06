#!/usr/bin/python
#!coding=utf-8
import jenkins
import sys
import time
import os
import xlrd

server = jenkins.Jenkins('http://172.17.1.1:8080/jenkins/', username='shiminde', password='passwd')

my_job = server.get_job_config('QA/GIT-MULTIBRANCH-EXAMPLE')

print my_job
