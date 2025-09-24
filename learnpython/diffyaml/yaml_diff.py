import yaml
from deepdiff import DeepDiff
import sys

def load_yaml_file(file_path):
    """Load and parse a YAML file."""
    try:
        with open(file_path, 'r') as file:
            return yaml.safe_load(file)
    except Exception as e:
        print(f"Error loading {file_path}: {e}")
        sys.exit(1)

def compare_yaml_files(file1_path, file2_path):
    """Compare two YAML files and print their differences."""
    # Load the YAML files
    data1 = load_yaml_file(file1_path)
    data2 = load_yaml_file(file2_path)

    # Compute the difference using DeepDiff
    diff = DeepDiff(data1, data2, verbose_level=2)

    # Print the differences in a readable format
    if diff:
        print("Differences found between the YAML files:")
        for change_type, changes in diff.items():
            print(f"\n{change_type.replace('_', ' ').title()}:")
            for key, change in changes.items():
                if change_type == 'values_changed':
                    print(f"  {key}:")
                    print(f"    Old value: {change['old_value']}")
                    print(f"    New value: {change['new_value']}")
                elif change_type in ['items_added', 'items_removed']:
                    print(f"  {key}: {change}")
                else:
                    print(f"  {key}: {change}")
    else:
        print("No differences found between the YAML files.")

def main():
    """Main function to handle command-line arguments."""
    if len(sys.argv) != 3:
        print("Usage: python yaml_diff.py <file1.yaml> <file2.yaml>")
        sys.exit(1)

    file1_path = sys.argv[1]
    file2_path = sys.argv[2]
    compare_yaml_files(file1_path, file2_path)

if __name__ == "__main__":
    main()
