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

class JongoEvent(object):
    def __init__(self):
        self.handlers = set()

    def handle(self, handler):
        self.handlers.add(handler)
        return self

    def unhandle(self, handler):
        try:
            self.handlers.remove(handler)
        except:
            raise ValueError("Handler is not handling this event, so cannot remove it.")
        return self

    def fire(self, *args, **kargs):
        for handler in self.handlers:
            handler(*args, **kargs)

    def getHandlerCount(self):
        return len(self.handlers)

    __iadd__ = handle
    __isub__ = unhandle
    __call__ = fire
    __len__ = getHandlerCount

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
        self.headers = {"Content-type": "application/json"}
        self.response = Response()
        if type(params) is str:
            self.params = params
        else:
            self.params = json.dumps(params)

    def perform(self):
        conn = httplib.HTTPConnection(self.url)
        conn.request(self.method, self.path, self.params, self.headers)
        self.response.callback(conn.getresponse().read())

class ProxyError(Exception):
    def __init__(self, message):
        self.message = message

    def __str__(self):
        return repr(self.message)

class Proxy(object):
    def __init__(self, url, path, model):
        self.url = url
        self.path = path
        self.model = model

    def create(self, model_instance):
        request = Request(self.url, self.path, 'POST', model_instance.toJSON())
        request.perform()
        if not request.response.success:
            raise ProxyError(request.response.error)

    def readAll(self):
        data = []
        request = Request(self.url, self.path)
        request.perform()
        if request.response.success:
            for response in request.response.data:
                model_instance = self.model()
                model_instance.map_response_data(response)
                data.append(model_instance)
        else:
            raise ProxyError(request.response.error)
        return data

    def read(self, id):
        instance_path = "%s/%d" % (self.path, id)
        request = Request(self.url, instance_path)
        request.perform()
        model_instance = None
        if request.response.success:
            model_instance = self.model()
            model_instance.map_response_data(request.response.data[0])
        return model_instance

    def update(self, instance):
        instance_path = "%s/%d" % (self.path, instance.id)
        request = Request(self.url, instance_path, 'PUT', instance.toJSON())
        request.perform()
        if not request.response.success:
            raise ProxyError(request.response.error)

    def delete(self, instance):
        instance_path = "%s/%d" % (self.path, instance.id)
        request = Request(self.url, instance_path, 'DELETE')
        request.perform()
        if not request.response.success:
            raise ProxyError(request.response.error)

class JongoStore(object):
    def __init__(self, proxy=None, model=None, autoLoad=False, autoSync=False, data=None, pageSize=25):
        self.proxy = proxy
        self.model = model
        self.autoLoad = autoLoad
        self.autoSync = autoSync
        self.data = data
        self.pageSize = pageSize
        self.page = 0

        if autoLoad:
            self.load()

    def add(self, model_instance):
        model_instance.set('ghost', True)
        self.data.append(model_instance)

    def remove(self, model_instance):
        if model_instance in self.data:
            i = self.data.index(model_instance)
            model_instance.dead = True
            self.data[i] = model_instance
        else:
            raise ValueError("The given model is not in the store")

    def update(self, model_instance):
        if model_instance in self.data:
            model_instance.ghost = False
            model_instance.dirty = True
            i = self.data.index(model_instance)
            self.data[i] = model_instance
        else:
            raise ValueError("The given model is not in the store")

    def getAt(self, index):
        return self.data[index]

    def getById(self, id):
        ret = None
        for i in self.data:
            if i.id == id:
                ret = i
        return ret

    def count(self):
        return len(self.data)

    def _get_limit(self):
        pass 

    def _get_offset(self):
        pass

    def filter(self, attr, value):
        new_data = []
        for i in self.data:
            if i.__dict__[attr] == value:
                new_data.append(i)

        if len(new_data) > 0:
            self.data = new_data

    def load(self):
        self.data = self.proxy.readAll()

    def sync(self):
        for instance in self.data:
            if instance.ghost:
                self.proxy.create(instance)
            elif instance.dirty:
                self.proxy.update(instance)
            elif instance.dead:
                self.proxy.delete(instance)
        self.load()

class JongoModel(object):
    def __init__(self, proxy=None, id=None, idCol='id', ghost=False, dirty=False, dead=False):
        self.proxy = proxy
        self.id = id
        self.idCol = idCol
        self.ghost = ghost
        self.dirty = dirty
        self.dead = dead

    def _get_unmappable_values(self):
        unmap = [ "proxy", "id", "idCol", "ghost", "dirty", "dead", self.idCol ] 
        return unmap

    def set(self, attr, value):
        if attr not in self._get_unmappable_values():
            self.dirty = True
        self.__dict__[attr] = value 

    def get(self, attr):
        return self.__dict__[attr]

    def create(self):
        if not proxy:
            raise ProxyError("No proxy configured for this model")

    def read(self, id):
        if not proxy:
            raise ProxyError("No proxy configured for this model")

    def update(self, id, params):
        if not proxy:
            raise ProxyError("No proxy configured for this model")

    def delete(self, id):
        if not proxy:
            raise ProxyError("No proxy configured for this model")

    def map_response_data(self, data):
        #if self.idCol:
        #    me[self.idCol] = self.id
        for attr, value in self.__dict__.iteritems():
            if attr in data:
                self.__dict__[attr] = data[attr]
    
    def toJSON(self):
        me = {}
        for attr, value in self.__dict__.iteritems():
            if attr not in self._get_unmappable_values():
                me[attr] = value
        return json.dumps(me)

    def __str__(self):
        me = []
        for attr, value in self.__dict__.iteritems():
            me.append("%s:%s" % (attr, value))
        return " ".join(me)

