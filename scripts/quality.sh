#!/bin/bash
# scripts/quality.sh
# Unified Quality CLI for Project Mecanica
# Handles linting, fixing, auditing, and git hooks installation.

# Ensure we are in the project root
cd "$(dirname "$0")/.."

# --- 1. Maven Detection Logic (Centralized) ---
if [ -f "./mvnw" ]; then
    MVN_CMD="./mvnw"
elif command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
elif [ -f "./mvn-docker.sh" ]; then
    MVN_CMD="./mvn-docker.sh"
else
    echo "❌ No Maven found (mvn, mvnw, or mvn-docker.sh). Cannot run quality tools."
    exit 1
fi
# ----------------------------------------------

# Function: Auto-Fix Formatting and Refactoring
fix_code() {
    echo "🧹 Running OpenRewrite and Spotless to fix formatting, cleanup code, and remove unused imports..."
    $MVN_CMD rewrite:run spotless:apply
    echo "✅ Fix complete."
}

# Function: Audit Code (Warnings)
check_code() {
    echo "🔍 Running static analysis tools (Checkstyle, PMD, SpotBugs)..."
    $MVN_CMD checkstyle:check pmd:check spotbugs:check -DskipTests

    echo "📊 Analysis complete. Check reports in target/ directory:"
    echo "- Checkstyle: target/checkstyle-result.xml"
    echo "- PMD: target/pmd.xml"
    echo "- SpotBugs: target/spotbugsXml.xml"
}

# Function: Install Git Hook
install_hook() {
    HOOKS_DIR="scripts/githooks"
    echo "⚓ Configuring Git to use hooks from $HOOKS_DIR..."

    # Ensure the directory exists (it should, as it's versioned)
    if [ ! -d "$HOOKS_DIR" ]; then
        echo "❌ Error: $HOOKS_DIR directory not found. Please ensure the project structure is intact."
        exit 1
    fi

    # Configure git core.hooksPath
    git config core.hooksPath "$HOOKS_DIR"

    # Ensure the hook script is executable
    chmod +x "$HOOKS_DIR/pre-commit"

    if [ $? -eq 0 ]; then
        echo "✅ Git hooks configured successfully! Git will now look for hooks in $HOOKS_DIR."
    else
        echo "❌ Failed to configure git hooks."
        exit 1
    fi
}

# Function: Pre-commit Hook Logic (Internal)
run_hook_logic() {
    echo "🚀 [Pre-commit] Running Quality Checks & Auto-Fixes..."

    # 1. Attempt Auto-Fix (Rewrite + Spotless)
    echo "🛠️  [Pre-commit] Running OpenRewrite & Spotless Apply..."
    $MVN_CMD rewrite:run spotless:apply
    if [ $? -ne 0 ]; then
        echo "❌ Auto-fix execution failed (Compilation error?). Fix errors and try again."
        exit 1
    fi

    # 2. Check for changes (Abort if fixed)
    if ! git diff --quiet; then
        echo "⚠️  [Pre-commit] Code was modified by Auto-Fix (Rewrite/Spotless)."
        echo "The following files were updated:"
        git diff --name-only
        echo ""
        echo "❌ Commit aborted. Please review the changes, 'git add' the fixed files, and try committing again."
        exit 1
    fi

    # 3. Verify Style (Checkstyle)
    echo "🔍 [Pre-commit] Verifying Code Style..."
    $MVN_CMD checkstyle:check
    if [ $? -ne 0 ]; then
        echo "❌ Checkstyle failed. Please fix the reported issues."
        exit 1
    fi

    echo "✅ [Pre-commit] All checks passed!"
}

# --- 2. Command Routing ---

case "$1" in
  "fix")
    fix_code
    ;;
  "check")
    check_code
    ;;
  "install")
    install_hook
    ;;
  "pre-commit-hook")
    run_hook_logic
    ;;
  *)
    echo "Usage: $0 {fix|check|install}"
    echo ""
    echo "Commands:"
    echo "  fix      - Auto-format code and remove unused imports"
    echo "  check    - Run static analysis (PMD, Checkstyle, SpotBugs)"
    echo "  install  - Install the git pre-commit hook"
    exit 1
    ;;
esac
