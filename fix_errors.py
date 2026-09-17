import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.saveable.rememberSaveable
"""

if "import androidx.navigation.compose.NavHost" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imports)
else:
    if "import androidx.compose.runtime.saveable.rememberSaveable" not in content:
        content = content.replace("import androidx.navigation.compose.NavHost", "import androidx.navigation.compose.NavHost\nimport androidx.compose.runtime.saveable.rememberSaveable\n")

# Assignments Screen parameter fix
content = content.replace("onNavigateToAssignment = { assignmentId -> navController.navigate(\"assignment/$assignmentId\") }", 
                          "onNavigateToAssignmentDetail = { assignmentId -> navController.navigate(\"assignment/$assignmentId\") }")

# Profile Screen back callback fix: it expects `onBack: () -> Unit`
# Oh wait, `onBack` in ProfileScreen might not expect a String?
# The error was: `Argument type mismatch: actual type is 'Function0<Unit>', but 'Function1<String, Unit>' was expected.`
# Wait, ProfileScreen takes `onSwitchRole: (String) -> Unit` !! Let's check!

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
