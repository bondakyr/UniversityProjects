#include "crusoe.hpp"

#include <algorithm>
#include <limits>
#include <queue>


vertex::vertex() : xy(0, 0), name(""), c_forward("#000000") {}

vertex::vertex(std::string str, int x, int y, std::string col)
    : xy(x, y), name(std::move(str)), c_forward(std::move(col)) {}

bool vertex::add_neighbour(size_t vv, const std::string& col) {
    for (const auto& neighbour : neighbours) {
        if (neighbour.first == vv) {
            return false;
        }
    }
    neighbours.emplace_back(vv, col);
    return true;
}

std::vector<std::pair<size_t, std::string>> vertex::get_neighbour() const {
    return neighbours;
}

std::pair<int, int> vertex::get_xy() const {
    return xy;
}

void vertex::set_color(const std::string& col) {
    c_forward = col;
}

std::string vertex::get_color() const {
    return c_forward;
}

void vertex::set_edge_color(size_t vv, const std::string& col) {
    for (auto& neighbour : neighbours) {
        if (neighbour.first == vv) {
            neighbour.second = col;
            return;
        }
    }
}

std::string vertex::get_edge_color(size_t vv) {
    for (const auto& neighbour : neighbours) {
        if (neighbour.first == vv) {
            return neighbour.second;
        }
    }
    return "#FFFFFF";
}


size_t graph::graph_comp::count() const {
    return components.size();
}

std::vector<size_t> graph::graph_comp::get_component(size_t index) const {
    if (index >= components.size()) {
        throw std::out_of_range("Invalid component index");
    }
    return components[index];
}

void graph::add_vertex(int x, int y, const std::string& col) {
    vertices.emplace_back(std::to_string(num_elem), x, y, col);
    ++num_elem;
}

void graph::add_edge(size_t v1, size_t v2, const std::string& col) {
    if (v1 >= num_elem || v2 >= num_elem) {
        return;
    }
    vertices[v1].add_neighbour(v2, col);
    vertices[v2].add_neighbour(v1, col);
}


bool graph::is_edge(size_t v1, size_t v2) const {
    if (v1 >= num_elem || v2 >= num_elem) {
        return false;
    }
    for (const auto& neighbour : vertices[v1].get_neighbour()) {
        if (neighbour.first == v2) {
            return true;
        }
    }
    return false;
}

std::string graph::edge_color(size_t v1, size_t v2) const {
    if (v1 >= vertices.size() || v2 >= vertices.size()) {
        return "#FFFFFF";
    }
    const vertex& vert = vertices[v1];

        return const_cast<vertex&>(vert).get_edge_color(v2);
    }

std::string graph::vertex_color(size_t v1) const {
    if (v1 >= num_elem) {
        return "#FFFFFF";
    }
    return vertices[v1].get_color();
}

void graph::set_vertex_color(size_t v1, const std::string& col) {
    if (v1 < num_elem) {
        vertices[v1].set_color(col);
    }
}

void graph::set_edge_color(size_t v1, size_t v2, const std::string& col) {
    if (v1 >= num_elem || v2 >= num_elem) {
        return;
    }
    vertices[v1].set_edge_color(v2, col);
    vertices[v2].set_edge_color(v1, col);
}

bool graph::empty() const {
    return num_elem == 0;
}

size_t graph::size() const {
    return num_elem;
}

size_t graph::num_edge() const {
    size_t edge_count = 0;
    for (const auto& vertex : vertices) {
        edge_count += vertex.get_neighbour().size();
    }
    return edge_count / 2;
}

vertex graph::get_vertex(size_t num) const {
    if (num >= num_elem) {
        throw std::out_of_range("Invalid vertex index");
    }
    return vertices[num];
}

void graph::color_component(std::vector<size_t> cmp, const std::string& col) {
    for (size_t vertex : cmp) {
        set_vertex_color(vertex, col);
        for (const auto& neighbour : vertices[vertex].get_neighbour()) {
            set_edge_color(vertex, neighbour.first, col);
        }
    }
}

void graph::color_path(std::vector<size_t> path, const std::string& color) {
    for (size_t i = 0; i + 1 < path.size(); ++i) {
        set_edge_color(path[i], path[i + 1], color);
    }
}


void graph::is_achievable(size_t from, std::vector<size_t>& achieved) {
    if (from >= num_elem) {
        return;
    }
    achieved.clear();
    std::vector<bool> visited(num_elem, false);
    std::queue<size_t> q;
    q.push(from);
    visited[from] = true;

    while (!q.empty()) {
        size_t current = q.front();
        q.pop();
        achieved.push_back(current);

        for (const auto& neighbour : vertices[current].get_neighbour()) {
            size_t neighbour_index = neighbour.first;
            if (!visited[neighbour_index]) {
                visited[neighbour_index] = true;
                q.push(neighbour_index);
            }
        }
    }
}

std::vector<size_t> graph::path(size_t v1, size_t v2) {
    if (v1 >= num_elem || v2 >= num_elem) {
        return {};
    }

    std::vector<bool> visited(num_elem, false);
    std::vector<size_t> parent(num_elem, num_elem);
    std::queue<size_t> q;

    q.push(v1);
    visited[v1] = true;

    while (!q.empty()) {
        size_t current = q.front();
        q.pop();

        if (current == v2) {
            break;
        }

        for (const auto& neighbour : vertices[current].get_neighbour()) {
            size_t neighbour_index = neighbour.first;
            if (!visited[neighbour_index]) {
                visited[neighbour_index] = true;
                parent[neighbour_index] = current;
                q.push(neighbour_index);
            }
        }
    }

    if (!visited[v2]) {
        return {};
    }

    std::vector<size_t> result;
    for (size_t at = v2; at != num_elem; at = parent[at]) {
        result.push_back(at);
    }
    std::reverse(result.begin(), result.end());
    return result;
}

graph::graph_comp::graph_comp(graph& g) : gg(g) {
    std::vector<bool> visited(gg.size(), false);

    for (size_t i = 0; i < gg.size(); ++i) {
        if (!visited[i]) {
            std::vector<size_t> component;
            gg.is_achievable(i, component);
            components.push_back(component);
            for (size_t v : component) {
                visited[v] = true;
            }
        }
    }
}

void graph::graph_comp::color_componennts() {
    std::vector<std::string> colors{"red", "olive", "orange", "lightblue", "yellow", "pink", "cyan", "purple", "brown", "magenta"};
    for (size_t i = 0; i < components.size(); ++i) {
        if (components[i].size() > 1) {
            gg.color_component(components[i], colors[i % colors.size()]);
        }
    }
}


size_t graph::graph_comp::count_without_one() const {
    size_t count = 0;
    for (const auto& component : components) {
        if (component.size() > 1) {
            ++count;
        }
    }
    return count;
}

size_t graph::graph_comp::max_comp() const {
    size_t max_index = 0;
    size_t max_size = 0;
    for (size_t i = 0; i < components.size(); ++i) {
        if (components[i].size() > max_size) {
            max_size = components[i].size();
            max_index = i;
        }
    }
    return max_index;
}

size_t graph::graph_comp::size_of_comp(size_t i) const {
    if (i >= components.size()) {
        throw std::out_of_range("Invalid component index");
    }
    return components[i].size();
}

bool graph::graph_comp::same_comp(size_t v1, size_t v2) const {
    for (const auto& component : components) {
        bool found_v1 = std::find(component.begin(), component.end(), v1) != component.end();
        bool found_v2 = std::find(component.begin(), component.end(), v2) != component.end();
        if (found_v1 && found_v2) {
            return true;
        }
    }
    return false;
}

graph::graph_fence::graph_fence(graph& g, size_t vv, size_t distance) : gg(g) {
    if (vv >= gg.size()) return;

    std::queue<std::pair<size_t, size_t>> q;
    std::vector<bool> visited(gg.size(), false);

    q.emplace(vv, 0);
    visited[vv] = true;

    while (!q.empty()) {
        auto front = q.front(); // Замінюємо деструктуризацію
        size_t current = front.first;
        size_t dist = front.second;
        q.pop();

        if (dist > distance) continue;
        fence.push_back(current);

        for (const auto& neighbour : gg.get_vertex(current).get_neighbour()) {
            size_t neighbour_index = neighbour.first;
            if (!visited[neighbour_index]) {
                visited[neighbour_index] = true;
                q.emplace(neighbour_index, dist + 1);
            }
        }
    }

}

void graph::graph_fence::color_fence(const std::string& col) {
    for (size_t vertex : fence) {
        gg.set_vertex_color(vertex, col);
    }
}

size_t graph::graph_fence::count_stake() const {
    return fence.size();
}

size_t graph::graph_fence::get_stake(size_t i) const {
    if (i >= fence.size()) {
        return static_cast<size_t>(-1);
    }
    return fence[i];
}
