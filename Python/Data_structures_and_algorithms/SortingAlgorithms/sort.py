import sys
import io

stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8', newline='\n')
stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', newline='\n')
stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', newline='\n')

def die(msg):
    stderr.write(f"{msg}\n")
    stderr.flush()
    sys.exit(1)

def write_lines(lines):
    buf = io.StringIO()
    for line in lines:
        buf.write(f"{line}\n")
    stdout.write(buf.getvalue())
    stdout.flush()

def main():
    try:
        header_line = stdin.readline()
    except Exception:
        die("Error: Chybna hlavicka souboru!")

    parts = header_line.strip().split()
    if len(parts) != 3:
        die("Error: Chybna hlavicka souboru!")

    try:
        maximum, typ, virus = map(int, parts)
    except ValueError:
        die("Error: Chybna hlavicka souboru!")

    if maximum < 1:
        die("Error: Maximum neni kladne!")
    if typ not in {0, 1, 2}:
        die("Error: Neznamy typ razeni posloupnosti!")
    if virus not in {0, 1}:
        die("Error: Nelze urcit, zda posloupnost napadl virus!")

    a = []
    for line in stdin:
        line = line.strip()
        if line == '':
            continue
        try:
            val = int(line)
        except ValueError:
            die("Error: Prvek posloupnosti je mimo rozsah!")
        if val < 1 or val > maximum:
            die("Error: Prvek posloupnosti je mimo rozsah!")
        a.append(val)
        if len(a) > 2_000_000:
            die("Error: Posloupnost ma vic nez 2000000 prvku!")

    n = len(a)
    if virus == 0 and typ == 1:
        for i in range(1, n):
            if a[i - 1] > a[i]:
                die("Error: Posloupnost neni usporadana!")
        if n < 1000:
            die("Error: Posloupnost ma mene nez 1000 prvku!")
        write_lines(a)
        return

    if virus == 0 and typ == 2:
        for i in range(1, n):
            if a[i - 1] < a[i]:
                die("Error: Posloupnost neni usporadana!")
        if n < 1000:
            die("Error: Posloupnost ma mene nez 1000 prvku!")
        write_lines(reversed(a))
        return

    if maximum <= 2_000_000:
        if n < 1000:
            die("Error: Posloupnost ma mene nez 1000 prvku!")
        count = [0] * (maximum + 1)
        for v in a:
            count[v] += 1
        result = []
        for v in range(1, maximum + 1):
            result.extend([v] * count[v])
        write_lines(result)
        return

    if n < 1000:
        die("Error: Posloupnost ma mene nez 1000 prvku!")

    for shift in range(0, 32, 8):
        C = [0] * 256
        for v in a:
            C[(v >> shift) & 0xFF] += 1
        sum_ = 0
        for i in range(256):
            C[i], sum_ = sum_, sum_ + C[i]
        buf = [0] * n
        for v in a:
            idx = (v >> shift) & 0xFF
            buf[C[idx]] = v
            C[idx] += 1
        a = buf

    write_lines(a)


if __name__ == '__main__':
    main()
