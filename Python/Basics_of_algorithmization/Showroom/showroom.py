class Node:
    def __init__(self, data, nextNode=None, prevNode=None):
        self.nextNode = nextNode
        self.prevNode = prevNode
        self.data = data


class LinkedList:
    def __init__(self):
        self.head = None


class Car:  
    def __init__(self, identification, name, brand, price, active):
        self.identification = identification
        self.name = name
        self.brand = brand
        self.price = price
        self.active = active


db = LinkedList()

def init(cars):
     for car in cars:
         add(car)


def add(car):
    new_node = Node(car)
    if db.head is None or car.price < db.head.data.price or (car.price == db.head.data.price and car.identification < db.head.data.identification):
        new_node.nextNode = db.head
        db.head = new_node
    else:
        current = db.head
        while (
            current.nextNode is not None and
            (car.price > current.nextNode.data.price or
             (car.price == current.nextNode.data.price and car.identification > current.nextNode.data.identification))
        ):
            current = current.nextNode
        new_node.nextNode = current.nextNode
        current.nextNode = new_node



def updateName(identification, name): 
    
    current = db.head
    new = None 

    while current is not None:
        if current.data.identification == identification:
            current.data.name = name
            new = current.data.name
            break
        current = current.nextNode
    return new

def updateBrand(identification, brand):
    
    current = db.head
    br = None 

    while current is not None:
        if current.data.identification == identification:
            current.data.brand = brand
            br = current.data.brand
            break
        current = current.nextNode
    return br


def activateCar(identification):
    
    current = db.head
    act = None 

    while current is not None:
        if current.data.identification == identification:
            current.data.active = True
            act = current.data.active
            break
        current = current.nextNode
    return act
    

def deactivateCar(identification):
    
    current = db.head
    neact = None

    while current is not None:
        if current.data.identification == identification:
            current.data.active = False
            neact = current.data.active
            break
        current = current.nextNode
    return neact


def getDatabaseHead():
    if db.head is not None:
        return db.head
    else:
        return None 


def getDatabase():

    if db is not None:
        return db
    else:
        return None 


def calculateCarPrice():

    current = db.head
    total = 0

    while current is not None:
        if current.data.active:
            total += current.data.price
        current = current.nextNode
    return total


def clean():
    if(db.head):
        db.head = None
    else:
        return None

