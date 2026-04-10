class Edge:
    def __init__(self, source, target, weight):
        self.source = source
        self.target = target
        self.weight = weight

class Vertex:
    def __init__(self, id, name):
        self.id = id
        self.name = name
        self.edges = []
        self.minDistance = float('inf')
        self.previousVertex = None
        


class Dijkstra:
    def __init__(self):
        self.vertexes = []

    def computePath(self, sourceId):
        unvisited = set(self.vertexes)

        for vertex in self.vertexes:
            if vertex.id == sourceId:
                vertex.minDistance = 0
            else:
                vertex.minDistance = float('inf')

        while unvisited:
            current_vertex = min(unvisited, key=lambda v: v.minDistance)
            unvisited.remove(current_vertex)
            for edge in current_vertex.edges:
                neighbour_vertex = self.getVertexById(edge.target)
                new_distance = current_vertex.minDistance + edge.weight

                if new_distance < neighbour_vertex.minDistance:
                    neighbour_vertex.minDistance = new_distance
                    neighbour_vertex.previousVertex = current_vertex

    def getVertexById(self, id):
        for vertex in self.vertexes:
            if vertex.id == id:
                return vertex
        return None
    
    def getShortestPathTo(self, targetId):
        shortest_path = []
        target_vertex = None

        for vertex in self.vertexes:
            if vertex.id == targetId:
                target_vertex = vertex
                break

        if target_vertex is None or target_vertex.minDistance == float('inf'):
            return shortest_path

        while target_vertex:
            shortest_path.insert(0, target_vertex)
            target_vertex = target_vertex.previousVertex

        return shortest_path

    def createGraph(self, vertexes, edgesToVertexes):
        self.vertexes = vertexes
        for edge in edgesToVertexes:
            for vertex in self.vertexes:
                if vertex.id == edge.source:
                    neighbour_vertex = next((v for v in self.vertexes if v.id == edge.target), None)
                    if neighbour_vertex:
                        vertex.edges.append(edge)

    def resetDijkstra(self):
        if self.vertexes is not None:
            for vertex in self.vertexes:
                vertex.minDistance = float('inf')
                vertex.previousVertex = None

    def getVertexes(self):
        return self.vertexes if self.vertexes else []
