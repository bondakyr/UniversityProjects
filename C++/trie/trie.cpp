#include "trie.hpp"

#include <utility>
#include <algorithm>
#include <functional>

#include <iostream>
#include <queue>

trie::trie() : m_root(new trie_node()), m_size(0) {}

trie::~trie() {
    if (!m_root) {
        return;
    }
    std::queue<trie_node*> nodes;
    nodes.push(m_root);
    while (!nodes.empty()) {
        trie_node* current = nodes.front();
        nodes.pop();
        for (auto child : current->children) {
            if (child) nodes.push(child);
        }
        delete current;
    }
}



bool trie::insert(const std::string& str) {

    if (str.empty()) {

        if (!m_root->is_terminal) {
            m_root->is_terminal = true;
            ++m_size;
            return true;
        } else {

            return false;
        }
    }

    trie_node* current = m_root;
    bool is_new_word = false;
    for (char c : str) {
        unsigned char idx = static_cast<unsigned char>(c);
        if (!current->children[idx]) {
            current->children[idx] = new trie_node();
            current->children[idx]->parent = current;
            current->children[idx]->payload = c;
            is_new_word = true;
        }
        current = current->children[idx];
    }
    if (!current->is_terminal) {
        current->is_terminal = true;
        is_new_word = true;
        ++m_size;
    }
    return is_new_word;
}


bool trie::erase(const std::string& str) {
    trie_node* current = m_root;
    for (char c : str) {
        auto idx = static_cast<unsigned char>(c);
        if (!current->children[idx]) return false;
        current = current->children[idx];

    }
    if (!current->is_terminal) return false;

    current->is_terminal = false;
    m_size--;

    for (auto it = str.rbegin(); it != str.rend(); ++it) {
        trie_node* parent = current->parent;
        bool has_children = std::any_of(std::begin(current->children), std::end(current->children), [](trie_node* child) {
            return child != nullptr;
        });
        if (has_children || current->is_terminal) break;

        delete current;
        parent->children[*it] = nullptr;
        current = parent;
    }

    return true;
}

bool trie::contains(const std::string& str) const {
    const trie_node* current = m_root;
    for (char c : str) {
        auto idx = static_cast<unsigned char>(c);
        if (!current->children[idx]) return false;
        current = current->children[idx];

    }
    return current->is_terminal;
}

size_t trie::size() const {
    return m_size;
}

bool trie::empty() const {
    return m_size == 0;
}

std::vector<std::string> trie::search_by_prefix(const std::string& prefix) const {
    const trie_node* current = m_root;
    for (char c : prefix) {
        if (!current->children[c]) return {};
        current = current->children[c];
    }

    std::vector<std::string> results;
    std::string path = prefix;
    std::function<void(const trie_node*, std::string&)> dfs = [&](const trie_node* node, std::string& path) {
        if (node->is_terminal) results.push_back(path);
        for (char c = 0; c < num_chars; ++c) {
            if (node->children[c]) {
                path.push_back(c);
                dfs(node->children[c], path);
                path.pop_back();
            }
        }
    };
    dfs(current, path);
    return results;
}

std::vector<std::string> trie::get_prefixes(const std::string& str) const {
    std::vector<std::string> prefixes;

    if (m_root->is_terminal) {
        prefixes.push_back("");
    }

    const trie_node* current = m_root;
    std::string path;
    for (char c : str) {
        unsigned char idx = static_cast<unsigned char>(c);
        if (!current->children[idx]) {
            break;
        }
        current = current->children[idx];
        path.push_back(c);
        if (current->is_terminal) {
            prefixes.push_back(path);
        }
    }
    return prefixes;
}


void trie::swap(trie& rhs) {
    std::swap(m_root, rhs.m_root);
    std::swap(m_size, rhs.m_size);
}



bool trie::operator==(const trie& rhs) const {
    if (size() != rhs.size()) return false;

    auto left = std::vector<std::string>(begin(), end());
    auto right = std::vector<std::string>(rhs.begin(), rhs.end());
    return left == right;
}



trie::trie(const trie& rhs) : m_root(new trie_node()), m_size(rhs.m_size) {
    std::function<void(const trie_node*, trie_node*)> copy_nodes = [&](const trie_node* src, trie_node* dest) {
        for (size_t i = 0; i < num_chars; ++i) {
            if (src->children[i]) {
                dest->children[i] = new trie_node(*src->children[i]);
                dest->children[i]->parent = dest;
                copy_nodes(src->children[i], dest->children[i]);
            }
        }
    };

    if (rhs.m_root) {
        copy_nodes(rhs.m_root, m_root);
    }
}






bool trie::operator<(const trie& rhs) const {
    return std::lexicographical_compare(begin(), end(), rhs.begin(), rhs.end());
}




trie trie::operator&(const trie& rhs) const {
    trie result;

    std::function<void(const trie_node*, const trie_node*, trie_node*&)> intersect
        = [&](const trie_node* a, const trie_node* b, trie_node*& out) {
            if (!a || !b) return;
            if (!out) out = new trie_node();

            if (a->is_terminal && b->is_terminal) {
                out->is_terminal = true;
            }

            for (size_t i = 0; i < num_chars; i++) {
                if (a->children[i] && b->children[i]) {

                    intersect(a->children[i], b->children[i], out->children[i]);
                    if (out->children[i]) {
                        out->children[i]->parent = out;

                        out->children[i]->payload = static_cast<char>(i);
                    }
                }
            }
    };

    intersect(m_root, rhs.m_root, result.m_root);

    result.m_size = 0;
    if (result.m_root) {
        std::queue<trie_node*> q;
        q.push(result.m_root);
        while (!q.empty()) {
            auto node = q.front();
            q.pop();
            if (node->is_terminal) {
                result.m_size++;
            }
            for (auto child : node->children) {
                if (child) q.push(child);
            }
        }
    }

    return result;
}

trie::trie(const std::vector<std::string>& strings) : trie() {
    for (const auto& str : strings) {
        insert(str);
    }
}
trie& trie::operator=(trie&& rhs) {
    if (this != &rhs) {

        this->~trie();

        m_root = rhs.m_root;
        m_size = rhs.m_size;

        rhs.m_root = nullptr;
        rhs.m_size = 0;
    }
    return *this;
}



trie& trie::operator=(const trie& rhs) {
    if (this == &rhs) return *this;

    trie temp(rhs);
    swap(temp);
    return *this;
}

trie::const_iterator trie::begin() const {
    std::function<const trie_node*(const trie_node*)> find_first_terminal = [&](const trie_node* node) -> const trie_node* {
        if (!node) return nullptr;
        if (node->is_terminal) return node;
        for (size_t i = 0; i < num_chars; ++i) {
            const trie_node* child = node->children[i];
            if (child) {
                const trie_node* result = find_first_terminal(child);
                if (result) return result;
            }
        }
        return nullptr;
    };
    return const_iterator(find_first_terminal(m_root));
}



trie::const_iterator trie::end() const {
    return const_iterator();
}
trie::const_iterator::const_iterator(const trie_node* node) : current_node(node) {}

trie::const_iterator& trie::const_iterator::operator++() {
    if (!current_node) return *this;

    std::function<const trie_node*(const trie_node*)> find_next_terminal = [&](const trie_node* node) -> const trie_node* {
        for (size_t i = 0; i < num_chars; ++i) {
            const trie_node* child = node->children[i];
            if (child) {
                if (child->is_terminal) return child;
                const trie_node* result = find_next_terminal(child);
                if (result) return result;
            }
        }
        return nullptr;
    };

    const trie_node* next = find_next_terminal(current_node);
    if (next) {
        current_node = next;
        return *this;
    }
    const trie_node* parent = current_node->parent;
    while (parent) {
        for (size_t i = current_node->payload + 1; i < num_chars; ++i) {
            const trie_node* sibling = parent->children[i];
            if (sibling) {
                if (sibling->is_terminal) {
                    current_node = sibling;
                    return *this;
                }
                next = find_next_terminal(sibling);
                if (next) {
                    current_node = next;
                    return *this;
                }
            }
        }
        current_node = parent;
        parent = parent->parent;
    }
    current_node = nullptr;
    return *this;
}


trie trie::operator|(const trie& rhs) const {
    trie result(*this);
    for (auto it = rhs.begin(); it != rhs.end(); ++it) {
        result.insert(*it);
    }
    return result;
}


trie::const_iterator trie::const_iterator::operator++(int) {
    const_iterator temp = *this;
    ++(*this);
    return temp;
}
std::string trie::const_iterator::operator*() const {
    if (!current_node) return "";
    std::string result;
    const trie_node* current = current_node;
    while (current && current->parent) {
        result.push_back(current->payload);
        current = current->parent;
    }
    std::reverse(result.begin(), result.end());
    return result;
}


bool trie::const_iterator::operator==(const const_iterator& rhs) const {
    return current_node == rhs.current_node;
}

bool trie::const_iterator::operator!=(const const_iterator& rhs) const {
    return !(*this == rhs);
}

trie::trie(trie&& rhs)
    : m_root(rhs.m_root),
      m_size(rhs.m_size)
{
    rhs.m_root = nullptr;
    rhs.m_size = 0;
}


bool operator!=(const trie& lhs, const trie& rhs) {
    return !(lhs == rhs);
}

bool operator>(const trie& lhs, const trie& rhs) {
    return rhs < lhs;
}
bool operator<=(const trie& lhs, const trie& rhs) {
    return !(rhs < lhs);
}
bool operator>=(const trie& lhs, const trie& rhs) {
    return !(lhs < rhs);
}
std::ostream& operator<<(std::ostream& out, const trie& t) {
    for (auto it = t.begin(); it != t.end(); ++it) {
        out << *it << "\n";
    }
    return out;
}





