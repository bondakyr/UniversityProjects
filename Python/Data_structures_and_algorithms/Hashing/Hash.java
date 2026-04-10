import java.util.*;
import java.io.*;

public class Hash {

    static class Entry {
        String key;
        int count;

        Entry(String key, int count) {
            this.key = key;
            this.count = count;
        }
    }

    static class HashTable {
        private static final Entry TOMBSTONE = new Entry(null, 0);

        private Entry[] slots;
        private int count;
        private int m;
        private final int initialSize;

        HashTable(int size) {
            this.initialSize = size;
            this.m = size;
            this.slots = new Entry[m];
            this.count = 0;
        }

        private int charCode(char c) {
            if (c == ' ') return 31;
            if ('a' <= c && c <= 'z') return c - 'a' + 1;
            return 0;
        }

        private int hash(String key) {
            int h = 0, p = 1;
            for (char c : key.toCharArray()) {
                int code = charCode(c);
                h = (h + code * p) % m;
                p = (p * 32) % m;
            }
            return h;
        }

        private int findSlot(String key, boolean[] found) {
            int start = hash(key);
            Integer firstTomb = null;
            int idx = start;

            do {
                Entry slot = slots[idx];
                if (slot == null) {
                    found[0] = false;
                    return firstTomb != null ? firstTomb : idx;
                } else if (slot == TOMBSTONE) {
                    if (firstTomb == null) firstTomb = idx;
                } else if (slot.key.equals(key)) {
                    found[0] = true;
                    return idx;
                }

                idx = (idx + 1) % m;
            } while (idx != start);

            found[0] = false;
            return firstTomb != null ? firstTomb : idx;
        }

        private void resize(int newSize) {
            if (newSize < initialSize || newSize == m) return;

            Entry[] oldSlots = slots;
            slots = new Entry[newSize];
            m = newSize;
            count = 0;

            for (Entry e : oldSlots) {
                if (e != null && e != TOMBSTONE) {
                    boolean[] found = new boolean[1];
                    int idx = findSlot(e.key, found);
                    slots[idx] = new Entry(e.key, e.count);
                    count++;
                }
            }
        }

        public void insert(String key) {
            boolean[] found = new boolean[1];
            int idx = findSlot(key, found);
            if (found[0]) {
                slots[idx].count++;
            } else {
                slots[idx] = new Entry(key, 1);
                count++;
                if ((double) count / m >= 0.7) resize(m * 2);
            }
        }

        public void delete(String key) {
            boolean[] found = new boolean[1];
            int idx = findSlot(key, found);
            if (!found[0]) return;

            Entry e = slots[idx];
            e.count--;
            if (e.count == 0) {
                slots[idx] = TOMBSTONE;
                count--;
                if (m > initialSize && (double) count / m < 0.3)
                    resize(Math.max(initialSize, m / 2));
            }
        }

        public int[] get(String key) {
            boolean[] found = new boolean[1];
            int idx = findSlot(key, found);
            if (found[0]) return new int[]{idx, slots[idx].count};
            return new int[]{-1, 0};
        }

        public int getCount() {
            return count;
        }

        public int getSize() {
            return m;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String[] memberNames = { "Mirek", "Jarka", "Jindra", "Rychlonozka", "Cervenacek" };
        int[] initSizes = { 11, 11, 11, 11, 11 };
        List<HashTable> tables = new ArrayList<>();
        int selected = -1;
        boolean justAfterA = false;

        String line;
        List<String> allLines = new ArrayList<>();
        while ((line = reader.readLine()) != null) allLines.add(line.strip());

        int i = 0;
        if (i < allLines.size() && allLines.get(i).startsWith("#i")) {
            String[] parts = allLines.get(i).split("\\s+");
            for (int j = 1; j <= 5 && j < parts.length; j++) {
                try {
                    initSizes[j - 1] = Integer.parseInt(parts[j]);
                } catch (NumberFormatException ignored) {}
            }
            i++;
        }

        for (int sz : initSizes) tables.add(new HashTable(sz));

        while (i < allLines.size()) {
            line = allLines.get(i++).strip();
            if (line.isEmpty() || line.equals("...")) continue;

            if (!line.startsWith("#")) {
                if (selected != -1)
                    tables.get(selected).insert(line);
                continue;
            }

            String cmd = line;

            if (cmd.equals("#a")) {
                while (i < allLines.size() && !allLines.get(i).startsWith("#")) {
                    String msg = allLines.get(i++).strip();
                    if (!msg.isEmpty() && !msg.equals("...")) {
                        for (HashTable t : tables)
                            t.insert(msg);
                    }
                }
                selected = -1;
                justAfterA = true;
                continue;
            }

            if (cmd.matches("#\\d")) {
                int idx = Integer.parseInt(cmd.substring(1)) - 1;
                if (idx >= 0 && idx < 5)
                    selected = idx;
                else {
                    System.err.println("Error: Chybny vstup!");
                    selected = -1;
                }
                justAfterA = false;
                continue;
            }

            if (cmd.equals("#p") || cmd.equals("#d")) {
                boolean isPrint = cmd.equals("#p");
                if (selected == -1) {
                    System.err.println("Error: Chybny vstup!");
                    if (justAfterA) {
                        while (i < allLines.size() && !allLines.get(i).startsWith("#")) {
                            String msg = allLines.get(i++).strip();
                            if (!msg.isEmpty() && !msg.equals("...")) {
                                for (HashTable t : tables)
                                    t.insert(msg);
                            }
                        }
                    }
                    continue;
                }

                HashTable tbl = tables.get(selected);
                if (isPrint) {
                    System.out.println(memberNames[selected]);
                    System.out.printf("\t%d %d%n", tbl.getSize(), tbl.getCount());
                    while (i < allLines.size() && !allLines.get(i).startsWith("#")) {
                        String q = allLines.get(i++).strip();
                        if (!q.isEmpty() && !q.equals("...")) {
                            int[] res = tbl.get(q);
                            System.out.printf("\t%s %d %d%n", q, res[0], res[1]);
                        }
                    }
                } else {
                    while (i < allLines.size() && !allLines.get(i).startsWith("#")) {
                        String q = allLines.get(i++).strip();
                        if (!q.isEmpty() && !q.equals("...")) {
                            tbl.delete(q);
                        }
                    }
                }
                justAfterA = false;
                continue;
            }

            System.err.println("Error: Chybny vstup!");
            justAfterA = false;
        }
    }
}
