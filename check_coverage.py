import csv
import sys

def check_coverage(file_path):
    print("Checking coverage from:", file_path)
    low_coverage_classes = []
    
    try:
        with open(file_path, 'r') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                group = row['GROUP']
                pkg = row['PACKAGE']
                clazz = row['CLASS']
                
                missed_instructions = int(row['INSTRUCTION_MISSED'])
                covered_instructions = int(row['INSTRUCTION_COVERED'])
                total_instructions = missed_instructions + covered_instructions
                
                missed_branches = int(row['BRANCH_MISSED'])
                covered_branches = int(row['BRANCH_COVERED'])
                total_branches = missed_branches + covered_branches
                
                if total_instructions > 0:
                    instruction_coverage = (covered_instructions / total_instructions) * 100
                else:
                    instruction_coverage = 100.0
                    
                if total_branches > 0:
                    branch_coverage = (covered_branches / total_branches) * 100
                else:
                    branch_coverage = 100.0 # If no branches, consider covered if instructions are covered
                    if total_instructions == 0:
                         branch_coverage = 100.0

                if instruction_coverage < 100 or branch_coverage < 100:
                    low_coverage_classes.append({
                        'package': pkg,
                        'class': clazz,
                        'instruction_coverage': instruction_coverage,
                        'branch_coverage': branch_coverage,
                        'missed_instructions': missed_instructions,
                        'missed_branches': missed_branches
                    })

        if not low_coverage_classes:
            print("🎉 All classes have 100% coverage!")
        else:
            print(f"⚠️ Found {len(low_coverage_classes)} classes with less than 100% coverage:")
            for item in low_coverage_classes:
                print(f"  - {item['package']}.{item['class']}: Instructions: {item['instruction_coverage']:.2f}%, Branches: {item['branch_coverage']:.2f}%")
                
    except Exception as e:
        print(f"Error reading CSV: {e}")

if __name__ == "__main__":
    check_coverage(sys.argv[1])
