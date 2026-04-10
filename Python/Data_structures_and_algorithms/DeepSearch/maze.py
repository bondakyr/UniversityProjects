
import sys

def main():

    if len(sys.argv) > 1:
        try:
            with open(sys.argv[1], 'r') as f:
                lines = f.read().splitlines()
        except Exception:
            sys.stderr.write("Error: Nepodarilo se nacist vstupni soubor!\n")
            sys.exit(1)
    else:
        lines = sys.stdin.read().splitlines()

    if not lines:
        return

    width = len(lines[0])
    for row in lines:
        if len(row) != width:
            sys.stderr.write("Error: Bludiste neni obdelnikove!\n")
            sys.exit(1)

    if width < 2 or lines[0][1] != '.':
        sys.stderr.write("Error: Vstup neni vlevo nahore!\n")
        sys.exit(1)

    if width < 2 or lines[-1][-2] != '.':
        sys.stderr.write("Error: Vystup neni vpravo dole!\n")
        sys.exit(1)

    if width < 5 or width > 100:
        sys.stderr.write("Error: Sirka bludiste je mimo rozsah!\n")
        sys.exit(1)

    height = len(lines)
    if height < 5 or height > 50:
        sys.stderr.write("Error: Delka bludiste je mimo rozsah!\n")
        sys.exit(1)

    for row in lines:
        for ch in row:
            if ch not in ('#', '.'):
                sys.stderr.write("Error: Bludiste obsahuje nezname znaky!\n")
                sys.exit(1)

    for i, ch in enumerate(lines[0]):
        if i == 1:
            if ch != '.':
                sys.stderr.write("Error: Vstup neni vlevo nahore!\n")
                sys.exit(1)
        else:
            if ch != '#':
                sys.stderr.write("Error: Bludiste neni oplocene!\n")
                sys.exit(1)

    for i, ch in enumerate(lines[-1]):
        if i == width - 2:
            if ch != '.':
                sys.stderr.write("Error: Vystup neni vpravo dole!\n")
                sys.exit(1)
        else:
            if ch != '#':
                sys.stderr.write("Error: Bludiste neni oplocene!\n")
                sys.exit(1)

    for row in lines:
        if row[0] != '#' or row[-1] != '#':
            sys.stderr.write("Error: Bludiste neni oplocene!\n")
            sys.exit(1)

    maze = [list(row) for row in lines]

    start = (0, 1)
    end = (height - 1, width - 2)

    def find_path(blocked=None):
        if blocked is None:
            blocked = set()
        stack = [(start, [start])]
        visited = set()
        while stack:
            (r, c), path = stack.pop()
            if (r, c) == end:
                return path
            if (r, c) in visited:
                continue
            visited.add((r, c))

            for dr, dc in [(-1, 0), (0, -1), (0, 1), (1, 0)]:
                nr, nc = r + dr, c + dc
                if 0 <= nr < height and 0 <= nc < width:
                    if (nr, nc) in blocked:
                        continue
                    if maze[nr][nc] == '.':
                        stack.append(((nr, nc), path + [(nr, nc)]))
        return None

    first_path = find_path()
    if first_path is None:
        sys.stderr.write("Error: Cesta neexistuje!\n")
        sys.exit(1)

    mandatory = {start, end}
    for cell in first_path:
        if cell in (start, end):
            continue

        blocked = {cell}
        if find_path(blocked) is None:
            mandatory.add(cell)

    for r, c in mandatory:
        maze[r][c] = '!'

    for row in maze:
        print("".join(row))

if __name__ == '__main__':
    main()
