# CppEditor

Standalone Android code editor widget (extracted from AndroLua_pro).

## What you get
- `CppEditor` (full-featured editor View)
- Complete `com.myopicmobile.textwarrior` engine (syntax highlighting, autocomplete, undo/redo, search, etc.)
- Defaults to **C/C++** syntax (`LanguageC`)

## How to turn this into an Android Library (AAR)

1. In Android Studio: **File → New → New Module → Android Library**
2. Copy the folder `app/src/main/java/com/` into your library module's `src/main/java/`
3. (Recommended) Rename the package from `com.androlua` to something like `com.cppeditor` (update imports if you do)
4. In your library module's `build.gradle`:
   ```gradle
   android {
       compileSdkVersion 34
       defaultConfig {
           minSdkVersion 21
           targetSdkVersion 34
       }
   }
   ```
5. Build: `./gradlew :yourlibrary:assembleRelease`

## Basic usage (in your app)

```java
CppEditor editor = new CppEditor(this);
editor.setDark(true);                    // dark theme
editor.setTextSize(16);
editor.setText("// Your C++ code here\n#include <iostream>\n\nint main() {\n    std::cout << \"Hello\";\n}");
editor.setWordWrap(true);

// Optional: add extra autocomplete words
editor.addNames(new String[]{"std", "vector", "cout", "endl", "printf", "nullptr"});

setContentView(editor);
```

## Main methods

- `setText(String)`
- `getText()`, `getSelectedText()`
- `undo()`, `redo()`
- `search()`, `gotoLine()`, `findNext(String keyword)`
- `setDark(boolean)`
- Color customization: `setKeywordColor()`, `setStringColor()`, `setCommentColor()`, etc.
- `addNames(String[])` and `addPackage(String, String[])` for custom autocomplete

## Notes
- No Lua, no full AndroLua dependencies.
- Works as a normal Android View.
- For better C++ support you can edit `LanguageC.java` (add more keywords, operators, etc.).

Originally from Nsad123/ab (fork of nirenr/AndroLua_pro). Pruned & adapted June 2026.
