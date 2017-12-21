# Orders

```
// Example tree
     A
    / \
   B   C
  / \
 D   E
```

### scalameta/scalagen

- Leaf first
- Top to bottom of file (Left to right of tree)
- Multiple Annotations are processed left to right (outer to inner)
  - Note: This order is subject to change.

`D -> E -> B -> C -> A`

### scalameta/paradise and scalamacros/paradise

- Leaf first
- Bottom to top of file (right to left)
- Multiple Annotations processed right to left (inner to outer)

`C -> E -> D -> B -> A`

### scalameta/scalameta traverse/transform
- Root first
- Top to bottom (Left to right of tree)

`A -> B -> D -> E -> C`

## Generation

### Extension/Manipulation
- Result gets computed when tranforming the annotee
- Get added the AST when tranforming the annotee

### Companion and Transmutation
- Result gets computed when tranforming the annotee
- Result is added to the AST during *parent* tranformation,
before the parent has actually been tranformed

### Inputs
The input to a given generator is the current state of the Tree.

For example, when transforming `B`

It's children will be D' and E', the already tranformed versions of D and E
