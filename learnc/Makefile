# Compiler and flags
CC          := gcc
CFLAGS      := -Wall -Wextra -Isrc -O2
DEPFLAGS    = -MT $@ -MMD -MP -MF $(OBJ_DIR)/$*.d

# Targets
TARGET      := main_app
TEST_TARGET := test_app

# Directories
SRC_DIR     := src
TEST_DIR    := tests
OBJ_DIR     := obj
BIN_DIR     := bin

# Find all source files (excluding main.c for testing)
SRCS        := $(shell find $(SRC_DIR) -name '*.c')
TEST_SRCS   := $(TEST_DIR)/test_main.c $(filter-out $(SRC_DIR)/main.c, $(SRCS))

# Map source files to object files (Preserve src and tests directories inside obj/)
OBJS        := $(SRCS:%.c=$(OBJ_DIR)/%.o)
TEST_OBJS   := $(TEST_SRCS:%.c=$(OBJ_DIR)/%.o)

# Generate dependency file paths
DEPS        := $(OBJS:.o=.d) $(TEST_OBJS:.o=.d)

# Default target
all: $(TARGET) ${TEST_TARGET}

# Debug mode setup
debug: CFLAGS := -Wall -Wextra -Isrc -g -DDEBUG
debug: all

# Link main executable
$(TARGET): $(OBJS)
	@echo "Linking application: $(BIN_DIR)/$@"
	@mkdir -p $(BIN_DIR)
	$(CC) $(CFLAGS) $^ -o $(BIN_DIR)/$@

# Link test executable
$(TEST_TARGET): $(TEST_OBJS)
	@echo "Linking test suite: $(BIN_DIR)/$(TEST_TARGET)"
	$(CC) $(CFLAGS) $^ -o $(BIN_DIR)/$(TEST_TARGET)
	@echo "Running tests..."
	./$(BIN_DIR)/$(TEST_TARGET)

# CORRECTED RULE: Matches obj/src/ or obj/tests/ explicitly
$(OBJ_DIR)/%.o: %.c
	@mkdir -p $(dir $@)
	@echo "Compiling: $< -> $@"
	$(CC) $(CFLAGS) $(DEPFLAGS) -c $< -o $@

# Include generated dependency files (ignores errors on first build)
-include $(DEPS)

# Clean up all build artifacts
clean:
	@echo "Cleaning up..."
	rm -rf $(OBJ_DIR) $(BIN_DIR)

.PHONY: all debug test clean
