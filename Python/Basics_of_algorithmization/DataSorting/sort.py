

def sortNumbers(data, condition):


    if condition == 'ASC':
        for i in range(len(data) - 1):
            for j in range(0, len(data) - i - 1):
                if data[j] > data[j + 1]:
                    data[j], data[j + 1] = data[j + 1], data[j]
        


    if condition == 'DESC':
        for i in range(len(data) - 1):
            for j in range(0, len(data) - i - 1):
                if data[j] < data[j + 1]:
                    data[j], data[j + 1] = data[j + 1], data[j]


    return data

def sortData(weights, data, condition):

    if len(weights) > len(data) or len(weights) < len(data):
         eror()
    
    we = weights.copy()

    sortNumbers(we,condition)

    mas = []

    for i in range(len(weights)):
        for j in range(len(weights)):
            if we[i] == weights[j]:
                mas.append(data[j])
                weights[j] = None


    return  mas

def eror():
    raise ValueError('Invalid input data')