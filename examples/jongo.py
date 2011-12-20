#!/usr/bin/env python
#
# Copyright (C) 2011 Alejandro Ayuso
#
# This file is part of Jongo
#
# Jongo is free software: you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Jongo is distributed in the hope that it will
# be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
# of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with the Monocaffe Connection Manager. If not, see
# <http://www.gnu.org/licenses/>.
#

# An example Python script which uses json, httplib and urllib to do some
# operations on Jongo running in demo mode

import json
import httplib, urllib

class Response:
    def __init__(self):
        self.raw = ""
        self.success = False
        self.count = 0
        self.code = 0 
        self.response = []
        self.error = ""
        self.message = ""

    def __str__(self):
        return '{response}'

    def callback(self, buf):
        #print buf
        self.raw = json.loads(buf)
        self.code = self.raw['code']
        self.count = self.raw['count']
        if self.code is 200 or self.code is 201:
            self.process_success()
        else:
            self.process_failure()

    def process_success(self):
        self.success = True
        for i in self.raw['response']:
            self.response.append(i)

    def process_failure(self):
        self.success = False
        self.error = self.raw['error']
        self.message = self.raw['message']


class Request:
    def __init__(self, url, path, method='GET', params={}):
        self.url = url
        self.path = path
        self.method = method
        self.response = Response()
        #self.params = urllib.urlencode(params)
        self.params = json.dumps(params)
        self.headers = {"Content-type": "application/json"}

    def perform(self):
        conn = httplib.HTTPConnection(self.url)
        conn.request(self.method, self.path, self.params, self.headers)
        self.response.callback(conn.getresponse().read())

r = Request('localhost:8080','/jongo/car')
r.perform()
print "The GET request was %s success " % r.response.success

r = Request('localhost:8080','/jongo/car', 'POST', {'model':'K1', 'maker':'KIA', 'transmission':"Manual", 'newvalue':"23.00", 'fuel':"Diesel",'year':2011})
r.perform()
print "The POST request was %s success " % r.response.success

r = Request('localhost:8080','/jongo/car')
r.perform()
print "The GET request was %s success " % r.response.success

ids = []
for i in r.response.response:
    ids.append(i['cid'])

next_id = '/jongo/car/%d' % max(ids)

r = Request('localhost:8080', next_id, 'PUT', {'model':'K11'})
r.perform()
print "The PUT request was %s success " % r.response.success

r = Request('localhost:8080', next_id, 'DELETE')
r.perform()
print "The DELETE request was %s success " % r.response.success
