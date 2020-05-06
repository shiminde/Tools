#!/usr/bin/python
import json
data = { 'a' : 1, 'b' : 2, 'c' : 3, 'd' : 4, 'e' : 5 }
print json.dumps(data, sort_keys=True, indent=4, separators=(',', ': '))
print json
