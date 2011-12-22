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

class Response(object):
    def __init__(self):
        self.raw = ""
        self.success = False
        self.count = 0
        self.code = 0 
        self.data = []
        self.error = ""
        self.message = ""

    def __str__(self):
        return '{response}'

    def callback(self, buf):
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
            self.data.append(i)

    def process_failure(self):
        self.success = False
        self.error = self.raw['error']
        self.message = self.raw['message']

class Request(object):
    def __init__(self, url, path, method='GET', params={}):
        self.url = url
        self.path = path
        self.method = method
        self.response = Response()
        self.params = json.dumps(params)
        self.headers = {"Content-type": "application/json"}

    def perform(self):
        conn = httplib.HTTPConnection(self.url)
        conn.request(self.method, self.path, self.params, self.headers)
        self.response.callback(conn.getresponse().read())

class JongoStore(object):
    def __init__(self, url=None, model=None, autoLoad=False, autoSync=False, data=None):
        self.url = url
        self.model = model
        self.autoLoad = autoLoad
        self.autoSync = autoSync
        self.data = data

        if autoLoad:
            self.load()

    def load(self):
        self.data = []
        model_class = self.get_model()
        model_instance = model_class() 
        request = Request(self.url, model_instance.path)
        request.perform()
        if request.response.success:
            for response in request.response.data:
                model_instance = model_class()
                model_instance.map_response_data(response)
                self.data.append(model_instance)

    def sync(self):
        pass

    def get_model(self):
        parts = self.model.split('.')
        module = ".".join(parts[:-1])
        m = __import__( module )
        for comp in parts[1:]:
            m = getattr(m, comp)     
        return m

class JongoModel(object):
    def __init__(self, path=None, id=None, idCol='id', ghost=False, dirty=False):
        self.path = path
        self.id = id
        self.idCol = idCol
        self.ghost = ghost
        self.dirty = dirty

    def create(self):
        pass

    def read(self):
        pass

    def update(self):
        pass

    def delete(self):
        pass

    def map_response_data(self, data):
        for attr, value in self.__dict__.iteritems():
            if attr in data:
                self.__dict__[attr] = data[attr]
                #setattr(self.__class__, attr, data[attr])

    def __str__(self):
        me = []
        for attr, value in self.__dict__.iteritems():
            me.append("%s:%s" % (attr, value))
        return " ".join(me)

class User(JongoModel):
    def __init__(self, id=None, name=None, age=None):
        JongoModel.__init__(self)
        self.id = id
        self.name = name
        self.age = age
        self.path = "/jongo/user"

class UserStore(JongoStore):
    def __init__(self):
        JongoStore.__init__(self)
        self.url = "localhost:8080"
        self.model = "jongo.User"

if __name__ == '__main__':
    store = UserStore()
    store.load()
    for user in store.data:
        print user

#    r = Request('localhost:8080','/jongo/car')
#    r.perform()
#    print "The GET request was %s success " % r.response.success
#
#    r = Request('localhost:8080','/jongo/car', 'POST', {'model':'K1', 'maker':'KIA', 'transmission':"Manual", 'newvalue':"23.00", 'fuel':"Diesel",'year':2011})
#    r.perform()
#    print "The POST request was %s success " % r.response.success
#
#    r = Request('localhost:8080','/jongo/car')
#    r.perform()
#    print "The GET request was %s success " % r.response.success
#
#    ids = []
#    for i in r.response.response:
#        ids.append(i['cid'])
#
#    next_id = '/jongo/car/%d' % max(ids)
#
#    r = Request('localhost:8080', next_id, 'PUT', {'model':'K11'})
#    r.perform()
#    print "The PUT request was %s success " % r.response.success
#
#    r = Request('localhost:8080', next_id, 'DELETE')
#    r.perform()
#    print "The DELETE request was %s success " % r.response.success
