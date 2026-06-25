# In-Memory Student Database Engine — A Data Structures & Algorithms Project

A from-scratch, dependency-free Java engine that loads a real-world dataset into a **custom singly linked list**, compresses every column with a **hand-built Huffman codec**, indexes records with a **self-balancing Red-Black Tree**, and answers SQL-like queries using **four sorting algorithms implemented from first principles** (bubble, insertion, merge, and quick sort).

No frameworks. No `Collections.sort()`. No third-party libraries. Every core data structure — the linked list, the tree, the compression codec — is implemented by hand, with step-by-step trace logs and tree visualizations generated at runtime so you can watch the algorithms work.

> This repository began as a two-part university algorithms assignment. The README below documents it as what it actually is: a hand-rolled data structures and algorithms project that combines four classic DSA topics — linked lists, Huffman coding, red-black trees, and sorting algorithms — into a single working system.

---

## Table of Contents

- [Why this project is interesting](#why-this-project-is-interesting)
- [Architecture overview](#architecture-overview)
- [Data structures & algorithms implemented](#data-structures--algorithms-implemented)
- [Project structure](#project-structure)
- [The dataset](#the-dataset)
- [How a query flows through the system](#how-a-query-flows-through-the-system)
- [Query language](#query-language)
- [Getting started](#getting-started)
- [Example session](#example-session)
- [Generated artifacts](#generated-artifacts)
- [Design notes & trade-offs](#design-notes--trade-offs)
- [Possible extensions](#possible-extensions)
- [Authors](#authors)

---

## Why this project is interesting

Most "DSA assignment" repos implement one structure in isolation (a tree here, a sort there) and stop. This project chains four classical structures into a single pipeline, where the output of one becomes the input of the next:

```
CSV rows → Linked List → Huffman Codec → Red-Black Tree index → Sorted output
```

That means the Red-Black tree isn't keyed on a plain integer — it's keyed on a **Huffman-encoded bit-string of arbitrary column values** (strings, chars, booleans, ints), converted deterministically into a tree key. The sorting algorithms don't sort arrays — they sort the **custom linked list in place**, using Java reflection to generically compare any column on the `Student` record without writing one comparator per field.

## Architecture overview

```
┌─────────────────┐     ┌────────────────┐     ┌───────────────────┐
│ student-data.csv │ --> │  LinkedList    │ --> │   HuffmanCodec     │
│ (395 records)    │     │  (custom impl) │     │ (per-column codec) │
└─────────────────┘     └────────────────┘     └───────────────────┘
                                                          │
                                                          v
┌──────────────────┐     ┌────────────────┐     ┌───────────────────┐
│ student-op-*.csv │ <-- │  Sort engine   │ <-- │   Red-Black Tree    │
│ (query result)   │     │ (4 algorithms) │     │  (encoded WHERE key) │
└──────────────────┘     └────────────────┘     └───────────────────┘
```

`MemoryDatabase` is the orchestrator that wires all of this together and exposes a minimal SQL-style REPL on top of it.

## Data structures & algorithms implemented

| Component | File | What it does |
|---|---|---|
| **Singly Linked List** | `LinkedList.java` | Custom node-based list storing `Student` records; backs the entire in-memory table. |
| **Huffman Coding** | `HuffmanCodec.java` | Builds a binary Huffman tree per column using a min-heap (`PriorityQueue`) keyed on token frequency. Produces a token → bitstring codebook used both for compression and as deterministic tree keys. |
| **Red-Black Tree** | `RedBlackTree.java`, `RBTreeNode.java` | A full from-scratch RB-tree (insert, left/right rotation, recoloring, all 3 fixup cases and their mirror cases) used to index records by their `WHERE`-clause column(s). |
| **Bubble Sort** | `MemoryDatabase.bubbleSort` | Classic O(n²) adjacent-swap sort, adapted to operate on linked-list nodes. |
| **Insertion Sort** | `MemoryDatabase.insertionSort` | In-place insertion sort walking the list and re-linking nodes. |
| **Merge Sort** | `MemoryDatabase.mergeSort` | O(n log n) divide-and-conquer sort using the classic fast/slow pointer technique to find the list midpoint, then recursive merge. |
| **Quick Sort** | `MemoryDatabase.quickSort` | Linked-list adaptation of quicksort using partition-by-relinking instead of array swaps. |
| **Reflection-based generic comparison** | `Student.compare`, `FilteredStudent.compare` | Uses `java.lang.reflect.Field` so a single comparator works across all 30+ columns without per-field boilerplate. |
| **Mini SQL parser** | `MemoryDatabase.retrieveParameters` | A regex-based grammar that parses `SELECT ... FROM ... WHERE ... ORDER BY ... WITH ...` queries into structured parameters. |
| **Tree visualization** | `RBTreeVisualizer.java`, `RBTreeVisualization.java` | Exports the tree as Graphviz `.dot` and renders it directly to PNG via Java2D/Swing — no external graph libraries. |

## Project structure

```
.
├── Java/
│   ├── MemoryDatabase.java         # Entry point + orchestration (CSV I/O, sorting, query engine)
│   ├── LinkedList.java             # Custom singly linked list + Node
│   ├── Student.java                # Record model for one student row (31 columns)
│   ├── FilteredStudent.java        # Dynamic subset-of-columns view of a Student (via reflection)
│   ├── SelectParameters.java       # Parsed representation of a SELECT query
│   ├── HuffmanCodec.java           # Per-column Huffman encoder/decoder + ASCII tree printer
│   ├── RBTreeNode.java             # Red-Black tree node
│   ├── RedBlackTree.java           # Red-Black tree (insert, rotate, fixup, search) + step logging
│   ├── RBTreeVisualizer.java       # Graphviz .dot export
│   ├── RBTreeVisualization.java    # Swing/Java2D PNG renderer
│   └── student-data.csv            # Dataset (see below)
├── Python/
│   └── student-data.csv            # Same dataset, kept for any companion analysis/notebook work
├── RBTree.dot / Final.dot          # Sample exported Graphviz trees
├── Final.png                       # Sample rendered Red-Black tree
├── RB_LOG.txt                       # Sample step-by-step Red-Black tree trace from a prior run
└── AlgorithmsProject_MemoryDatabase.iml   # IntelliJ module file
```

> Note: the original IntelliJ module is named `AlgorithmsProject_MemoryDatabase` — that's the project's real identity. `course_project_2` is just the repository name left over from how it was originally submitted; see [Renaming the repo](#renaming-the-repo-optional) below if you'd like the repo name to match.

## The dataset

`student-data.csv` is the public **UCI "Student Performance"** dataset (395 student records from two Portuguese secondary schools, 30 attributes per student plus a derived `passed` label) — demographics, family background, study habits, and academic outcome fields like `absences`, `studytime`, `failures`, and `passed`. It's a convenient stand-in for "real" tabular data: enough categorical and numeric diversity to make the Huffman codec and reflection-based sorting non-trivial, and a public, well-known schema if you want to cross-check results.

The `absences` / `WHERE`-clause columns are what get indexed into the Red-Black tree, since that's the kind of lookup ("find every student with exactly N absences," or some other equality condition) the tree is built to answer in better-than-linear time.

## How a query flows through the system

1. **Load** — `MemoryDatabase.main` reads `student-data.csv` line by line and appends each row as a `Student` into the custom `LinkedList` (`read`).
2. **Profile** — `createHuffman()` walks every column of every record once, builds a frequency table per column, and constructs one Huffman tree per column via `HuffmanCodec.fromFrequencies`.
3. **Encode** — `encodeDatabase()` re-walks the list and stores the Huffman bitstring for every cell, keyed by column name.
4. **Parse the query** — the user types a `SELECT ... FROM ... WHERE ... ORDER BY ... WITH ...` line, which `retrieveParameters()` validates and parses against a fixed regex grammar into a `SelectParameters` object.
5. **Project columns** — `findStudent()` decodes only the selected columns back out of the encoded store and materializes `FilteredStudent` rows (a column subset view created entirely through reflection, with no per-column code).
6. **Index & filter** — `createRBTree()` builds a Red-Black tree keyed on the Huffman-encoded `WHERE` column(s), inserting every filtered row, then performs a tree search for the rows matching the `WHERE` value(s). Each insertion, rotation, and recoloring step is logged to `RB_LOG.txt`, and the final tree is rendered to `.dot` and `.png`.
7. **Sort** — the matching rows are sorted in place on the linked list using whichever of the four algorithms the query requested (`bubble_sort`, `insertion_sort`, `merge_sort`, or `quick_sort`), comparing on the requested `ORDER BY` column via reflection.
8. **Output** — the result is written to a timestamped `student-op-data_<timestamp>.csv`.

## Query language

The REPL accepts a single line matching this grammar:

```
select <col1>, <col2>, ... from <table> where <col> equal <value> [and <col> equal <value> ...] order by <col> ASC|DSC with <algorithm>.
```

Where `<algorithm>` is one of: `bubble_sort`, `insertion_sort`, `merge_sort`, `quick_sort`.

**Example:**

```
select school,sex,age,absences,passed from students where absences equal 6 order by age ASC with merge_sort.
```

This selects four columns, filters to students with exactly 6 absences (via the Red-Black tree), sorts the matches by age ascending using merge sort, and writes the result to disk.

## Getting started

### Prerequisites
- JDK 17+ (the code uses modern `switch` expressions and `var`/pattern-matching features)
- No build tool required — it's a flat set of `.java` files

### 1. Set the data directory

`MemoryDatabase.BASE_PATH` is currently hardcoded to a Windows path:

```java
public static final String BASE_PATH = "D:\\Algorithms\\CourseProject-2\\Data";
```

Update this to wherever you keep `student-data.csv` locally (e.g. the `Java/` folder itself), and make sure `student-data.csv` lives in that directory. This is the one piece of setup the project needs before it will run on a fresh machine.

### 2. Compile

```bash
cd Java
javac *.java
```

### 3. Run

```bash
java MemoryDatabase
```

You'll be prompted for a query:

```
Write the select query
SQL>
```

Type a query matching the [query language](#query-language) above and press Enter.

## Example session

```
$ java MemoryDatabase
Write the select query
SQL>
select school,sex,age,absences,passed from students where absences equal 6 order by age ASC with merge_sort.
Refer the created log files for Huffman and Red-black trees
Output save to student-op-data06252026_141022.csv
```

This produces:
- `student-op-data<timestamp>.csv` — the filtered, sorted result set
- `RB_LOG.txt` — every insertion, rotation, and recoloring step of the Red-Black tree build, plus an ASCII tree snapshot after each fixup
- `HUFFMAN_LOG.txt` — the frequency table and merge order for every column's Huffman tree
- `Final.dot` / `RB_TreeFinal.png` — the completed tree, as a Graphviz source file and a rendered image

## Generated artifacts

This repo ships sample output from a prior run so you can see what the engine produces without running it yourself:

- **`RB_LOG.txt`** — a full trace of one Red-Black tree build: every `insert`, every `leftRotate`/`rightRotate`, every fixup case (and its mirror), with an ASCII rendering of the tree after each structural change.
- **`Final.dot` / `RBTree.dot`** — Graphviz definitions of the final tree state.
- **`Final.png`** — the tree rendered directly by `RBTreeVisualization` (pure Java2D/Swing, no Graphviz binary needed at runtime — `RBTreeVisualizer` produces the `.dot` file separately if you do want to render via Graphviz).

## Design notes & trade-offs

A few deliberate choices worth calling out, since they're the kind of thing a reviewer or instructor would ask about:

- **Why key the Red-Black tree on a Huffman bitstring instead of the raw value?** It lets the same tree implementation index on *any* combination of columns — strings, chars, booleans, numbers — by funneling them all through one deterministic `bits → int` mapping (`rbKeyFromBits`), rather than writing a different comparator per data type.
- **Why reflection instead of getters?** `Student` has 30 fields. Reflection (`Field.get`/`Field.set`) lets `compare()`, `convertFieldToColumn()`, and the Huffman frequency pass work generically across every column with one method instead of 30 hand-written comparators.
- **Why duplicate the dataset under both `Java/` and `Python/`?** The Python copy is kept for any exploratory/companion analysis outside the Java engine; the engine itself only reads from `Java/student-data.csv`.
- **Hash collisions in `rbKeyFromBits`:** bitstrings longer than 31 bits fall back to `String.hashCode()` rather than a `BigInteger` key, which is a pragmatic (if theoretically collision-prone) choice the code calls out explicitly in a comment.
- **The `Java*.zip` files** in the repo root look like point-in-time submission snapshots and aren't needed to build or run the project — the live source of truth is the `Java/` directory.

## Possible extensions

If you want to keep developing this as a portfolio piece:
- Replace the hardcoded `BASE_PATH` with a relative path or CLI argument
- Add a small JUnit suite around `RedBlackTree` (insert/search invariants) and each sort
- Support range queries (`absences between 2 and 10`) instead of only equality
- Add Big-O / empirical timing comparisons across the four sort algorithms on this dataset
- Swap the Swing-based PNG renderer for the Graphviz `.dot` output by default (drop the AWT dependency)

### Renaming the repo (optional)

GitHub repository names can't be changed through code, only through the repo's **Settings → General → Repository name** page (or `gh repo rename` via the CLI). Given the actual content, something like `student-rbtree-database`, `dsa-memory-database`, or `huffman-rbtree-student-db` would describe it more accurately than `course_project_2`.

## Authors

- Kowshick Srinivasan — linked list, Red-Black tree, student model, query engine, visualization
- Qingyun PU — Huffman codec, database orchestration

---

*Originally submitted as a two-part university algorithms course project ("Hw2" and "course project-2"); documented here as a standalone data structures and algorithms showcase.*
