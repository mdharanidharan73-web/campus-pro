import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    new_lines.append(line)
    if 'containerColor = MaterialTheme.colorScheme.background,' in line:
        pass

# Actually this is too complex. I will just download the original file if I can, or restore it.
