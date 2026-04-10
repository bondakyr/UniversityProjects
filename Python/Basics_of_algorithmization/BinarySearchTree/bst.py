class Node:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None

class BinarySearchTree:
    def __init__(self):
        self.root = None
        self.current = 0

    def insert(self, value):
        self.root = self._insert(self.root, value)

    def _insert(self, root, value):
        if root is None:
            return Node(value)
        if value < root.value:
            root.left = self._insert(root.left, value)
        elif value > root.value:
            root.right = self._insert(root.right, value)
        return root

    def fromArray(self, arr):
        for value in arr:
            self.insert(value)

    def search(self, value):
        return self._search(self.root, value)

    def _search(self, root, value):
        self.current = 0
        while root is not None:
            self.current += 1
            if value == root.value:
                return True
            elif value < root.value:
                root = root.left
            else:
                root = root.right
        return False

    def min(self):
        return self._min(self.root)

    def _min(self, root):
        self.current = 1
        if root is None:
            return None
        while root.left is not None:
            self.current += 1
            root = root.left
        return root.value

    def max(self):
        return self._max(self.root)

    def _max(self, root):
        self.current = 1
        if root is None:
            return None
        while root.right is not None:
            self.current += 1
            root = root.right
        return root.value

    def visitedNodes(self):
        return self.current
