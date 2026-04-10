#include "telescope.hpp"
#include <istream>
#include <fstream>
#include <utility>
#include <vector>
#include <string>
#include <sstream>
#include <stdexcept>
#include <iomanip>
#include <algorithm>

std::pair<size_t, size_t> parse_matrix(std::istream& in) {
    std::string line;
    size_t rows = 0;
    size_t cols = 0;

    while (std::getline(in, line)) {
        if (line.empty()) continue;

        std::istringstream iss(line);
        size_t current_cols = 0;
        int value;

        while (iss >> value) {
            current_cols++;
        }

        if (rows == 0) {
            cols = current_cols;
        } else if (current_cols != cols) {
            throw std::invalid_argument("Inconsistent number of columns in rows.");
        }

        rows++;
    }

    return {rows, cols};
}

std::vector<int> parse_matrix(std::istream& in, const std::pair<size_t, size_t>& m_size) {
    std::vector<int> vec;
    vec.reserve(m_size.first * m_size.second);

    std::string line;
    size_t expected_rows = m_size.first;
    size_t expected_cols = m_size.second;

    size_t rows = 0;

    while (std::getline(in, line)) {
        if (line.empty()) continue;

        std::istringstream iss(line);
        int value;
        size_t current_cols = 0;

        while (iss >> value) {
            vec.push_back(value);
            current_cols++;
        }

        if (current_cols != expected_cols) {
            throw std::invalid_argument("Inconsistent number of columns in rows.");
        }

        rows++;
    }

    if (rows != expected_rows) {
        throw std::invalid_argument("Number of rows does not match the expected size.");
    }

    return vec;
}

void print_matrix(std::ostream& out, const std::pair<size_t, size_t>& m_size, const std::vector<int>& vec) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (vec.empty() || rows == 0 || cols == 0) {
        return;
    }

    size_t max_width = 0;
    for (int num : vec) {
        max_width = std::max(max_width, static_cast<size_t>(std::to_string(num).length()));
    }
    max_width += 2;
    int width = max_width + 1;

    out << std::string(cols * width + 1, '-') << "\n";

    for (size_t i = 0; i < rows; ++i) {
        out << "|";
        for (size_t j = 0; j < cols; ++j) {
            out << std::setw(max_width - 1) << vec[i * cols + j] << " |";
        }
        out << "\n";
    }
    out << std::string(cols * width + 1, '-') << "\n";
}
std::vector<unsigned char> parse_stream(std::istream& in, const std::pair<size_t, size_t>& m_size) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;
    std::vector<unsigned char> vec;
    vec.reserve(rows * cols);

    char ch;
    size_t count = 0;

    while (in.get(ch)) {
        if (!isspace(ch) || ch == ' ') {
            vec.push_back(static_cast<unsigned char>(ch));
            count++;
        }
    }

    if (count != rows * cols) {
        throw std::invalid_argument("Inconsistent number of elements in the matrix.");
    }

    return vec;
}
void rotate_down(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (rows == 0 || cols == 0) return;

    std::vector<unsigned char> temp(vec.end() - cols, vec.end());
    for (size_t i = rows - 1; i > 0; --i) {
        std::copy(vec.begin() + (i - 1) * cols, vec.begin() + i * cols, vec.begin() + i * cols);
    }
    std::copy(temp.begin(), temp.end(), vec.begin());
}

void rotate_down(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec, int step) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (step == 0 || rows == 0) return;

    if (step < 0) {
        step = rows - (-step % rows);
        if (step == rows) step = 0;
    }

    std::vector<unsigned char> rotated(vec.size());

    for (size_t i = 0; i < rows; ++i) {
        size_t new_row = (i + step) % rows;
        for (size_t j = 0; j < cols; ++j) {
            rotated[new_row * cols + j] = vec[i * cols + j];
        }
    }

    vec = rotated;
}


void rotate_right(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (rows == 0 || cols == 0) return;

    for (size_t i = 0; i < rows; ++i) {
        unsigned char last = vec[i * cols + cols - 1];
        for (size_t j = cols - 1; j > 0; --j) {
            vec[i * cols + j] = vec[i * cols + j - 1];
        }
        vec[i * cols] = last;
    }
}

void rotate_right(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec, int step) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (cols == 0 || step == 0) return;

    if (step < 0) {
        step = cols - (-step % cols);
        if (step == cols) step = 0;
    }

    std::vector<unsigned char> rotated(vec.size());

    for (size_t j = 0; j < cols; ++j) {
        size_t new_col = (j + step) % cols;
        for (size_t i = 0; i < rows; ++i) {
            rotated[i * cols + new_col] = vec[i * cols + j];
        }
    }

    vec = rotated;
}

void swap_points(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec, const Point& p1, const Point& p2) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (p1.x >= m_size.second || p1.y >= m_size.first ||
        p2.x >= m_size.second || p2.y >= m_size.first) {
        throw std::invalid_argument("");
    }

    size_t ind1 = p1.y * cols + p1.x;
    size_t ind2 = p2.y * cols + p2.x;

    std::swap(vec[ind1], vec[ind2]);
}

void swap_points(const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec, const Point& p1, const Point& p2, const Point& delta) {
    size_t rows = m_size.first;
    size_t cols = m_size.second;

    if (p1.x + delta.x > cols || p1.y + delta.y > rows ||
        p2.x + delta.x > cols || p2.y + delta.y > rows) {
        throw std::invalid_argument("Rectangles are out of matrix bounds.");
        }

    bool overlap = !(p1.x + delta.x <= p2.x || p2.x + delta.x <= p1.x ||
                     p1.y + delta.y <= p2.y || p2.y + delta.y <= p1.y);

    if (overlap) {
        throw std::invalid_argument("Rectangles overlap.");
    }

    for (size_t i = 0; i < delta.y; ++i) {
        for (size_t j = 0; j < delta.x; ++j) {
            std::swap(vec[(p1.y + i) * cols + (p1.x + j)], vec[(p2.y + i) * cols + (p2.x + j)]);
        }
    }
}

void decode_picture(const std::string& file, const std::pair<size_t, size_t>& m_size, std::vector<unsigned char>& vec) {
    std::ifstream infile(file);
    std::string line;

    while (std::getline(infile, line)) {
        std::istringstream iss(line);
        char command;
        iss >> command;

        if (command == 'r' || command == 'l' || command == 'd' || command == 'u') {
            int step = 1;
            iss >> step;
            step = (command == 'l' || command == 'u') ? -step : step;

            if (command == 'r' || command == 'l') rotate_right(m_size, vec, step);
            else rotate_down(m_size, vec, step);

        } else if (command == 's') {
            size_t x1, y1, x2, y2, dx = 1, dy = 1;
            iss >> x1 >> y1 >> x2 >> y2;

            if (iss >> dx >> dy) {
                swap_points(m_size, vec, Point(x1, y1), Point(x2, y2), Point(dx, dy));
            } else {
                swap_points(m_size, vec, Point(x1, y1), Point(x2, y2));
            }
        }
    }
}
