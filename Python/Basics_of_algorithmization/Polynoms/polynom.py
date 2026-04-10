
def polyEval(poly, x):

    res = 0

    for i, a in enumerate(poly):
        res += a * (x ** i)

    return res


def polySum(poly1, poly2):

    poly3  = [0] * max(len(poly1),len(poly2))

    for i in range (max(len(poly1),len(poly2))):
        if i < len(poly1):
            poly3[i] += poly1[i]
        if i < len(poly2):
            poly3[i] += poly2[i]

    while poly3[-1] == 0:
        poly3.pop()

    return poly3


def polyMultiply(poly1, poly2):

    poly3  = [0] * (len(poly1) + len(poly2) - 1)
    
    for i in range (len(poly1)):
        for j in range (len(poly2)):
            poly3[i + j] += poly1[i] * poly2[j]  

    return poly3

