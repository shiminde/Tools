#!/usr/bin/python
import xlrd
import sys
import os
import subprocess
file=sys.argv[1]
data = xlrd.open_workbook(file)
table = data.sheets()[3]
nrows = table.nrows
cols = table.ncols
devlist = []
mainlist = []
X=""
#print cols
#print table.merged_cells	
for i in range(nrows):
		value = table.row_values(i)
		if "group" not in value and "project" not in value:
			print value


"""
if onetitle != "" and onetitle != "group" and onetitle != "project":
print onetitle             #1 group Service,WEB
#name = table.row_values(i)[2]
	
"""
