#!/usr/bin/env python3
"""
Simple verification script for the Maintainer Android project.
Checks for common issues and missing files.
"""

import os
import re
from pathlib import Path

def main():
    print("Maintainer Project Verification")
    print("=" * 40)

    project_root = Path(".")
    app_dir = project_root / "app"

    # Check basic project structure
    print("\nProject Structure:")
    required_files = [
        "build.gradle.kts",
        "settings.gradle.kts",
        "gradle.properties",
        "app/build.gradle.kts",
        "app/src/main/AndroidManifest.xml",
        "app/src/main/java/com/maintainer/app/MainActivity.kt",
        "app/src/main/java/com/maintainer/app/MaintainerApplication.kt"
    ]

    for file_path in required_files:
        full_path = project_root / file_path
        status = "[OK]" if full_path.exists() else "[MISSING]"
        print(f"  {status} {file_path}")

    # Check Kotlin files
    print(f"\nKotlin Files:")
    kt_files = list(project_root.rglob("*.kt"))
    print(f"  Found {len(kt_files)} Kotlin files")

    # Check for package declarations
    print(f"\nPackage Structure:")
    package_pattern = r'package\s+com\.maintainer\.app'
    correct_packages = 0

    for kt_file in kt_files:
        try:
            content = kt_file.read_text(encoding='utf-8')
            if re.search(package_pattern, content):
                correct_packages += 1
        except Exception as e:
            print(f"  [WARNING] Error reading {kt_file}: {e}")

    print(f"  {correct_packages}/{len(kt_files)} files have correct package structure")

    # Check resource files
    print(f"\nResource Files:")
    res_dir = app_dir / "src/main/res"
    if res_dir.exists():
        xml_files = list(res_dir.rglob("*.xml"))
        print(f"  Found {len(xml_files)} XML resource files")

        # Check for required resource files
        required_resources = [
            "values/strings.xml",
            "values/colors.xml",
            "values/themes.xml",
            "values/integers.xml"
        ]

        for resource in required_resources:
            resource_path = res_dir / resource
            status = "[OK]" if resource_path.exists() else "[MISSING]"
            print(f"    {status} {resource}")

    # Check for potential compilation issues
    print(f"\nPotential Issues:")
    issues = []

    # Check for missing imports (basic check)
    for kt_file in kt_files:
        try:
            content = kt_file.read_text(encoding='utf-8')

            # Check for common missing imports
            if 'androidx.compose' in content and 'import androidx.compose' not in content:
                issues.append(f"Possible missing Compose imports in {kt_file.name}")

            if 'Room' in content and 'androidx.room' not in content:
                issues.append(f"Possible missing Room imports in {kt_file.name}")

        except Exception:
            continue

    if issues:
        for issue in issues[:5]:  # Show first 5 issues
            print(f"  [WARNING] {issue}")
        if len(issues) > 5:
            print(f"  ... and {len(issues) - 5} more issues")
    else:
        print("  [OK] No obvious issues detected!")

    # Summary
    print(f"\nSummary:")
    print(f"  • {len(kt_files)} Kotlin source files")
    print(f"  • {len(list(res_dir.rglob('*.xml'))) if res_dir.exists() else 0} XML resource files")
    print(f"  • Package structure: {correct_packages}/{len(kt_files)} correct")
    print(f"  • Potential issues: {len(issues)}")

    print(f"\nNext Steps:")
    print(f"  1. Import project in Android Studio")
    print(f"  2. Add google-services.json for Google APIs")
    print(f"  3. Sync project with Gradle files")
    print(f"  4. Run './gradlew build' to check compilation")
    print(f"  5. Fix any remaining compilation errors")

if __name__ == "__main__":
    main()