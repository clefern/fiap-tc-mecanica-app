
import os

def find_missing_tests(src_root, test_root):
    missing_tests = []
    
    # Walk through src directory
    for root, _, files in os.walk(src_root):
        for file in files:
            if file.endswith(".java") and file != "MecanicaApplication.java": # Skip main class
                # Get relative path from src_root
                rel_path = os.path.relpath(root, src_root)
                
                # Construct expected test path
                class_name = file[:-5] # Remove .java
                test_file_name = f"{class_name}Test.java"
                
                # Check if test file exists in test_root (allowing for package structure match)
                # We expect the package structure to be mirrored
                expected_test_path = os.path.join(test_root, rel_path, test_file_name)
                
                if not os.path.exists(expected_test_path):
                     # Try to find it anywhere in test_root just in case structure is slightly different
                    found = False
                    for t_root, _, t_files in os.walk(test_root):
                        if test_file_name in t_files:
                            found = True
                            break
                    
                    if not found:
                         # Also check for IT (Integration Test) suffix
                        it_test_file_name = f"{class_name}IT.java"
                        for t_root, _, t_files in os.walk(test_root):
                            if it_test_file_name in t_files:
                                found = True
                                break

                    if not found:
                        missing_tests.append(os.path.join(rel_path, file))

    return missing_tests

if __name__ == "__main__":
    src_dir = "/Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/main/java"
    test_dir = "/Users/cleberfernandes/Workspace/Personal/fiap-arquitetura-soft/techChallenge/mecanica/src/test/java"
    
    missing = find_missing_tests(src_dir, test_dir)
    
    print("Files without corresponding Test class:")
    for file in missing:
        print(file)
