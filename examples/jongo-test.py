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

import jongo

class User(jongo.JongoModel):
    def __init__(self, id=None, name=None, age=None):
        jongo.JongoModel.__init__(self)
        self.id = id
        self.name = name
        self.age = age

class UserStore(jongo.JongoStore):
    def __init__(self):
        jongo.JongoStore.__init__(self)
        self.model = User

class Car(jongo.JongoModel):
    def __init__(self, id=None, model=None, maker=None, fuel=None, transmission=None):
        jongo.JongoModel.__init__(self)
        self.id = id
        self.idCol = "cid"
        self.model = model
        self.maker = maker
        self.fuel = fuel
        self.transmission = transmission

class CarStore(jongo.JongoStore):
    def __init__(self):
        jongo.JongoStore.__init__(self)
        self.model = Car
        self.proxy = jongo.Proxy("localhost:8080","/jongo/car", Car)

if __name__ == '__main__':
    store = UserStore()
    store.proxy = jongo.Proxy("localhost:8080","/jongo/user", User)
    store.load()
    for user in store.data:
        print user.toJSON()

    print "Create a user"
    u1 = User(None, 'kkk', 16)
    store.add(u1)

    u1 = store.getAt(store.count() - 1)
    assert u1.ghost == True
    assert u1.dirty == False
    print "Before sync, the user instance is a ghost. This means it doesn't have a value in the db"
    for user in store.data:
        print user

    print "Now we do the sync and the user should not be a ghost any more"
    store.sync()
    for user in store.data:
        print user

    print "Get the last user, probably the one we created"
    u1 = store.getAt(store.count() - 1)

    print "Let's change its name"
    u1.name = "ttt"
    store.update(u1)

    print "Before calling sync, the user should be dirty"
    for user in store.data:
        print user

    print "After sync, we have the user with the new name and it's not dirty"
    store.sync()
    for user in store.data:
        print user

    u1 = store.getAt(store.count() - 1)
    print u1
    print "To delete a user, we don't remove it from the store. It will be marked as dead"
    store.remove(u1)
    for user in store.data:
        print user

    print "When the sync is performed, the element is removed from the db and from the store"
    store.sync()
    for user in store.data:
        print user


    print "Now with the cars which have a custom id which is mapped to our column ID"
    carstore = CarStore()
    carstore.load()
    for car in carstore.data:
        print car

    c1 = Car(None, "206cc", "Peugeot", "Gasoline", "Manual")
    carstore.add(c1)

    for car in carstore.data:
        print car

    carstore.sync()
    for car in carstore.data:
        print car

    c1 = carstore.getAt(carstore.count() - 1) 
    c1.model = "206"
    c1.maker = "PPegoushn"
    carstore.update(c1)
    for car in carstore.data:
        print car
    carstore.sync()
    for car in carstore.data:
        print car

    # We need to refresh the object since it has changed after the sync
    c1 = carstore.getAt(carstore.count() - 1) 
    carstore.remove(c1)
    for car in carstore.data:
        print car
    carstore.sync()
    for car in carstore.data:
        print car
