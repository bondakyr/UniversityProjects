
class Food:
    def __init__(self, name, expiration):
        self.name = name
        self.expiration = expiration


def openFridge(fridge):
    print("Following items are in Homer's fridge:")
    for food in fridge:
        print("{0} (expires in: {1} days)".format(
            str(food.name), str(food.expiration))
        )
    print("")


def maxExpirationDay(fridge):

    max_date = 0

    if not fridge:
         return -1
    else:
        for food in fridge:
            max_date = max(food.expiration for food in fridge)
        return max_date 


def histogramOfExpirations(fridge):

    if not fridge:
        return []
    
    max_expiration = max(food.expiration for food in fridge)
    histogram = [0] * (max_expiration + 1)
    
    for food in fridge:
        if 0 <= food.expiration <= max_expiration:
            histogram[food.expiration] += 1
    
    return histogram


def cumulativeSum(histogram):

    new_histogram = histogram[:]
        
    for i in range(1, len(histogram)):
        new_histogram[i] += new_histogram[i - 1]
    
    return new_histogram


def sortFoodInFridge(fridge):

    sorted_fridge = [None] * len(fridge)
    histogram = histogramOfExpirations(fridge)
    cum_sum = cumulativeSum(histogram)
    
    for food in fridge:
        poslnd = cum_sum[food.expiration] - 1
        sorted_fridge[poslnd] = food
        cum_sum[food.expiration] -= 1
        
    return sorted_fridge

def reverseFridge(fridge):

    return fridge[::-1]

def eatFood(name, fridge):

    new_fridge = fridge[:]
    found = False
    min_expiration = float('inf')
    index_to_remove = -1

    for index, food in enumerate(new_fridge):
        if food.name == name:
            found = True
            if food.expiration < min_expiration:
                min_expiration = food.expiration
                index_to_remove = index
    
    if found:
        if index_to_remove != -1:
            del new_fridge[index_to_remove]
        return new_fridge
    else:
        return new_fridge
        
