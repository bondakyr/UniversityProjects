def permutations(array):

    new_arr = []

    if len(array) <= 1:
        return [array]
    
    for i, n in enumerate(array):
        num = array[:i] + array[i + 1:]
        for j in permutations(num):
            new_arr.append([n] + j)
   
    return new_arr
