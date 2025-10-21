# Diff Config YAML and CRUD appropriate Flink SQL

1. Python code to find the delta between two configurations yaml files
2. Yaml file has the following structure:
   - `name`: string
   - `version`: string
   - `tables`: list of table objects
     - `name`: string
     - `columns`: list of column objects
       - `name`: name of the column
       - `type`: type of the column
       - `default`: default value of the column (optional)
   - `joins`: list of join conditions on the `table` objects to create new table
      - `name`: name of the join
      - `conditions`: list of join conditions
        - `lhs`: left hand side of the join condition
          - `name`: name of the table
          - `alias`: alias of the table
          - `column`: column name of the table
        - `rhs`: right hand side of the join condition
          - `name`: name of the table
          - `alias`: alias of the table
          - `column`: column name of the table
      - `result`: result table of the join
        - `name`: name of the result table
        - `columns`: list of columns in the result table
          - `name`: name of the column
          - `column`: column name of the table
3. Based on the delta, generate the appropriate Flink SQL statements to create the new views, tables and perform the joins. Below are the requirements
4. Create a suffix for new tables and views with a timestamp in the format `YYYYMMDDHHMMSS`
5. When there is delta in `tables`
  1. When there are column name changes in `tables`
    1. Create `alter view` statements with the following format:
        ```sql
        ALTER VIEW <view_name> AS SELECT <column_list> FROM <table_name>;
        ```
    2. Create `alter table` statements with the following format:
        ```sql
        ALTER TABLE <table_name> ADD COLUMN <column_name> <column_type>;
        ```
    3. Modify `execute statement set` statements for table inserts
    4. Modify `execute statement set` statements to perform the joins
    5. Update existing tables based on the delta
    6. Delete existing tables based on the delta
    7. Delete existing views based on the delta
    8. Delete existing joins based on the delta
  2. When columns are removed from `tables`
    1. Create `alter view` statements with the following format:
        ```sql
        ALTER VIEW <view_name> AS SELECT <column_list> FROM <table_name>;
        ```
    2. Create `alter table` statements with the following format:
        ```sql
        ALTER TABLE <table_name> DROP COLUMN <column_name>;
        ```
    3. Modify `execute statement set` statements for table inserts
    4. Modify `execute statement set` statements to perform the joins
    5. Update existing tables based on the delta
    6. Delete existing tables based on the delta
    7. Delete existing views based on the delta
    8. Delete existing joins based on the delta
  3. Column name changes are not supported
6. When there is delta in `joins`
  1. Modify `execute statement set` statements to perform the joins
  2. Delete existing joins based on the delta
