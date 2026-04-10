#!/usr/bin/env python3
import sys
from collections import deque
import heapq


def read_input():
    """Read and parse input matrix"""
    try:
        line = input().strip()
        n, m = map(int, line.split())

        matrix = []
        for i in range(n):
            row = list(map(int, input().split()))
            if len(row) != m:
                raise ValueError("Invalid row length")
            matrix.append(row)

        return n, m, matrix
    except:
        print("Error: Chybny vstup!", file=sys.stderr)
        sys.exit(1)


def get_neighbors(i, j, n, m):
    """Get valid neighboring coordinates"""
    neighbors = []
    for di, dj in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
        ni, nj = i + di, j + dj
        if 0 <= ni < n and 0 <= nj < m:
            neighbors.append((ni, nj))
    return neighbors


def solve_lift(n, m, matrix):
    """Find optimal path for lift using optimized DP"""
    # Use simple DP with single best state per cell
    # up[i][j] = (min_length, max_gradient_for_min_length, predecessor)
    up = [[None for _ in range(m)] for _ in range(n)]
    down = [[None for _ in range(m)] for _ in range(n)]

    # Initialize starting point
    up[0][0] = (0, 0, None)

    # Fill up matrix using DP
    for i in range(n):
        for j in range(m):
            if i == 0 and j == 0:
                continue

            best_length = float('inf')
            best_gradient = -1
            best_pred = None

            # Check left neighbor
            if j > 0 and up[i][j - 1] is not None:
                prev_length, prev_gradient, _ = up[i][j - 1]
                if matrix[i][j] > matrix[i][j - 1]:
                    length = prev_length + 1
                    gradient = max(prev_gradient, matrix[i][j] - matrix[i][j - 1])

                    if (length < best_length or
                            (length == best_length and gradient > best_gradient)):
                        best_length = length
                        best_gradient = gradient
                        best_pred = (i, j - 1)

            # Check top neighbor
            if i > 0 and up[i - 1][j] is not None:
                prev_length, prev_gradient, _ = up[i - 1][j]
                if matrix[i][j] > matrix[i - 1][j]:
                    length = prev_length + 1
                    gradient = max(prev_gradient, matrix[i][j] - matrix[i - 1][j])

                    if (length < best_length or
                            (length == best_length and gradient > best_gradient)):
                        best_length = length
                        best_gradient = gradient
                        best_pred = (i - 1, j)

            if best_length != float('inf'):
                up[i][j] = (best_length, best_gradient, best_pred)

    # Initialize end point for down matrix
    down[n - 1][m - 1] = (0, 0, None)

    # Fill down matrix (going backwards)
    for i in range(n - 1, -1, -1):
        for j in range(m - 1, -1, -1):
            if i == n - 1 and j == m - 1:
                continue

            best_length = float('inf')
            best_gradient = -1
            best_pred = None

            # Check right neighbor
            if j < m - 1 and down[i][j + 1] is not None:
                next_length, next_gradient, _ = down[i][j + 1]
                if matrix[i][j] > matrix[i][j + 1]:
                    length = next_length + 1
                    gradient = max(next_gradient, matrix[i][j] - matrix[i][j + 1])

                    if (length < best_length or
                            (length == best_length and gradient > best_gradient)):
                        best_length = length
                        best_gradient = gradient
                        best_pred = (i, j + 1)

            # Check bottom neighbor
            if i < n - 1 and down[i + 1][j] is not None:
                next_length, next_gradient, _ = down[i + 1][j]
                if matrix[i][j] > matrix[i + 1][j]:
                    length = next_length + 1
                    gradient = max(next_gradient, matrix[i][j] - matrix[i + 1][j])

                    if (length < best_length or
                            (length == best_length and gradient > best_gradient)):
                        best_length = length
                        best_gradient = gradient
                        best_pred = (i + 1, j)

            if best_length != float('inf'):
                down[i][j] = (best_length, best_gradient, best_pred)

    # Find best peak
    best_total_length = float('inf')
    best_peak = None
    best_total_gradient = -1
    best_peak_height = -1

    for i in range(n):
        for j in range(m):
            if up[i][j] is not None and down[i][j] is not None:
                up_length, up_gradient, _ = up[i][j]
                down_length, down_gradient, _ = down[i][j]

                total_length = up_length + down_length + 1
                total_gradient = max(up_gradient, down_gradient)
                peak_height = matrix[i][j]

                # Priority: 1) shorter length, 2) higher peak, 3) higher gradient
                better = False
                if total_length < best_total_length:
                    better = True
                elif total_length == best_total_length:
                    if peak_height > best_peak_height:
                        better = True
                    elif peak_height == best_peak_height and total_gradient > best_total_gradient:
                        better = True

                if better:
                    best_total_length = total_length
                    best_peak = (i, j)
                    best_total_gradient = total_gradient
                    best_peak_height = peak_height

    if best_peak is None:
        return None

    # Reconstruct path
    # Reconstruct up path
    curr = best_peak
    up_path = []
    while curr is not None:
        up_path.append(curr)
        if up[curr[0]][curr[1]] is None:
            break
        _, _, pred = up[curr[0]][curr[1]]
        curr = pred
    up_path.reverse()

    # Reconstruct down path
    curr = best_peak
    down_path = []
    if down[curr[0]][curr[1]] is not None:
        _, _, pred = down[curr[0]][curr[1]]
        curr = pred
        while curr is not None:
            down_path.append(curr)
            if down[curr[0]][curr[1]] is None:
                break
            _, _, pred = down[curr[0]][curr[1]]
            curr = pred

    path = up_path + down_path
    elevation_path = [matrix[i][j] for i, j in path]

    return len(path), elevation_path


def solve_piste(n, m, matrix):
    """Find optimal path for piste using optimized approach"""
    # Use priority queue for efficient exploration
    # State: (negative_length, negative_height, gradient, i, j, is_up_phase)

    # up[i][j] = (length, gradient, predecessor)
    up = [[None for _ in range(m)] for _ in range(n)]
    down = [[None for _ in range(m)] for _ in range(n)]

    # Calculate up paths using modified Dijkstra
    up[0][0] = (0, 0, None)
    # Priority: maximize length, then maximize height, then minimize gradient
    pq = [(-0, -matrix[0][0], 0, 0, 0)]  # (neg_length, neg_height, gradient, i, j)

    while pq:
        neg_length, neg_height, gradient, i, j = heapq.heappop(pq)
        length = -neg_length
        height = -neg_height

        if up[i][j] is None or up[i][j][0] < length:
            continue

        for ni, nj in get_neighbors(i, j, n, m):
            if matrix[ni][nj] > matrix[i][j]:  # Can go up
                new_length = length + 1
                new_gradient = max(gradient, matrix[ni][nj] - matrix[i][j])

                update = False
                if up[ni][nj] is None:
                    update = True
                else:
                    old_length, old_gradient, _ = up[ni][nj]
                    if (new_length > old_length or
                            (new_length == old_length and new_gradient < old_gradient)):
                        update = True

                if update:
                    up[ni][nj] = (new_length, new_gradient, (i, j))
                    heapq.heappush(pq, (-new_length, -matrix[ni][nj], new_gradient, ni, nj))

    # Calculate down paths
    down[n - 1][m - 1] = (0, 0, None)
    pq = [(-0, -matrix[n - 1][m - 1], 0, n - 1, m - 1)]

    while pq:
        neg_length, neg_height, gradient, i, j = heapq.heappop(pq)
        length = -neg_length
        height = -neg_height

        if down[i][j] is None or down[i][j][0] < length:
            continue

        for ni, nj in get_neighbors(i, j, n, m):
            if matrix[ni][nj] > matrix[i][j]:  # Can come down from higher point
                new_length = length + 1
                new_gradient = max(gradient, matrix[ni][nj] - matrix[i][j])

                update = False
                if down[ni][nj] is None:
                    update = True
                else:
                    old_length, old_gradient, _ = down[ni][nj]
                    if (new_length > old_length or
                            (new_length == old_length and new_gradient < old_gradient)):
                        update = True

                if update:
                    down[ni][nj] = (new_length, new_gradient, (i, j))
                    heapq.heappush(pq, (-new_length, -matrix[ni][nj], new_gradient, ni, nj))

    # Find best peak
    best_total_length = -1
    best_peak = None
    best_total_gradient = float('inf')
    best_peak_height = -1

    for i in range(n):
        for j in range(m):
            if up[i][j] is not None and down[i][j] is not None:
                up_length, up_gradient, _ = up[i][j]
                down_length, down_gradient, _ = down[i][j]

                total_length = up_length + down_length + 1
                total_gradient = max(up_gradient, down_gradient)
                peak_height = matrix[i][j]

                # Priority: 1) longer length, 2) higher peak, 3) lower gradient
                better = False
                if total_length > best_total_length:
                    better = True
                elif total_length == best_total_length:
                    if peak_height > best_peak_height:
                        better = True
                    elif peak_height == best_peak_height and total_gradient < best_total_gradient:
                        better = True

                if better:
                    best_total_length = total_length
                    best_peak = (i, j)
                    best_total_gradient = total_gradient
                    best_peak_height = peak_height

    if best_peak is None:
        return None

    # Reconstruct path
    # Reconstruct up path
    curr = best_peak
    up_path = []
    while curr is not None:
        up_path.append(curr)
        if up[curr[0]][curr[1]] is None:
            break
        _, _, pred = up[curr[0]][curr[1]]
        curr = pred
    up_path.reverse()

    # Reconstruct down path
    curr = best_peak
    down_path = []
    if down[curr[0]][curr[1]] is not None:
        _, _, pred = down[curr[0]][curr[1]]
        curr = pred
        while curr is not None:
            down_path.append(curr)
            if down[curr[0]][curr[1]] is None:
                break
            _, _, pred = down[curr[0]][curr[1]]
            curr = pred

    path = up_path + down_path
    elevation_path = [matrix[i][j] for i, j in path]

    return len(path), elevation_path


def main():
    # Parse command line arguments
    mode = None
    if len(sys.argv) > 1:
        mode = sys.argv[1]
        if mode not in ['lift', 'piste']:
            print("Error: Chybny vstup!", file=sys.stderr)
            sys.exit(1)

    # Read input
    n, m, matrix = read_input()

    # Solve based on mode
    if mode == 'lift' or mode is None:
        result = solve_lift(n, m, matrix)
        if result is None:
            print("Error: Cesta neexistuje!", file=sys.stderr)
            sys.exit(1)
        length, path = result
        print(length)
        print(' '.join(map(str, path)))

    if mode == 'piste' or mode is None:
        result = solve_piste(n, m, matrix)
        if result is None:
            print("Error: Cesta neexistuje!", file=sys.stderr)
            sys.exit(1)
        length, path = result
        print(length)
        print(' '.join(map(str, path)))


if __name__ == "__main__":
    main()