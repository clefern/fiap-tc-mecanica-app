import csv
import sys

def calculate_total_coverage(file_path):
    try:
        total_missed = 0
        total_covered = 0
        
        with open(file_path, 'r') as csvfile:
            reader = csv.DictReader(csvfile)
            for row in reader:
                total_missed += int(row['INSTRUCTION_MISSED'])
                total_covered += int(row['INSTRUCTION_COVERED'])
                
        total_instructions = total_missed + total_covered
        
        if total_instructions > 0:
            coverage = (total_covered / total_instructions) * 100
            print(f"Total Coverage: {coverage:.2f}%")
        else:
            print("Total Coverage: 0.00% (No instructions found)")
            
    except Exception as e:
        print(f"Error reading CSV: {e}")

if __name__ == "__main__":
    calculate_total_coverage(sys.argv[1])
