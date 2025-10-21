import yaml
from deepdiff import DeepDiff
import sys
from pydantic import BaseModel
from typing import Optional, List

class ResultColumn(BaseModel):
    name: str
    column: str

class Result(BaseModel):
    name: str
    columns: List[ResultColumn]

class ConditionSide(BaseModel):
    name: str
    alias: str
    column: str

class Condition(BaseModel):
    lhs: ConditionSide
    rhs: ConditionSide

class Join(BaseModel):
    name: str
    conditions: List[Condition]
    result: Result

class Column(BaseModel):
    name: str
    type: str
    default: Optional[str] = None

class Table(BaseModel):
    name: str
    columns: List[Column]

class DBName(BaseModel):
    name: str
    tables: List[Table]
    joins: List[Join]

def load_yaml_file(file_path):
    """Load and parse a YAML file."""
    try:
        with open(file_path, 'r') as file:
            return yaml.safe_load(file)
    except Exception as e:
        print(f"Error loading {file_path}: {e}")
        sys.exit(1)

def compare_yaml_files(file1_path, file2_path):
    """Compare two YAML files and return their data and differences."""
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

    return data1, data2, diff

def access_yaml_elements(data, file_name, keys_to_access=None):
    """Access specific elements from a YAML data object."""
    print(f"\nAccessing elements from {file_name}:")
    if keys_to_access is None:
        keys_to_access = []

    for key_path in keys_to_access:
        try:
            # Handle nested keys (e.g., 'address.street' or 'hobbies[0]')
            current = data
            parts = key_path.replace(']', '').split('[')
            print(f"  parts is {parts}")
            for part in parts:
                print(f"  part is {part}")
                if part.endswith('.'):
                    part = part[:-1]
                if part.isdigit():
                    current = current[int(part)]
                else:
                    current = current.get(part, None)
                if current is None:
                    print(f"  {key_path}: Not found")
                    #break
            else:
                print(f"  {key_path}: {current}")
        except (KeyError, IndexError, TypeError):
            print(f"  {key_path}: Invalid path or not found")

def access_elements_from_diffs(data1, data2, diff, file1_name, file2_name):
    """Access elements related to the differences found."""
    print("\nAccessing elements related to differences:")
    for change_type, changes in diff.items():
        for key in changes.keys():
            # Convert DeepDiff key (e.g., root['address']['street']) to a usable path
            key_path = key.replace("root['", "").replace("['", ".").replace("']['", ".").replace("']", "")
            print(f"\nDifference at: {key_path} ({change_type})")
            access_yaml_elements(data1, file1_name, [key_path])
            access_yaml_elements(data2, file2_name, [key_path])

def main():
    """Main function to handle command-line arguments."""
    if len(sys.argv) != 3:
        print("Usage: python yaml_diff_access.py <file1.yaml> <file2.yaml>")
        sys.exit(1)

    file1_path = sys.argv[1]
    file2_path = sys.argv[2]

    # Compare files and get the data and differences
    data1, data2, diff = compare_yaml_files(file1_path, file2_path)

    data1Object = DBName(**data1)
    data2Object = DBName(**data2)

    #
    # TODO: Use a combination of DeepDiff and pydantic data element access
    # - to find the actual diff
    # - action on it
    #
    print (data1Object.name)
    print (data2Object.name)

    # Example: Access specific elements from both YAML files
    # keys_to_access = ['config.tables[1].columns[1].name']
    # access_yaml_elements(data1, file1_path, keys_to_access)
    # access_yaml_elements(data2, file2_path, keys_to_access)

    # Access elements related to the differences
    # access_elements_from_diffs(data1, data2, diff, file1_path, file2_path)

if __name__ == "__main__":
    main()
