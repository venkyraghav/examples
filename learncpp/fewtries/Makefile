# Compiler and flags
CXX         := clang++
CXXFLAGS      := -Wall -Wextra -Isrc -Iinclude -O2
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
SRCS        := $(shell find $(SRC_DIR) -name '*.cpp')
TEST_SRCS   := $(TEST_DIR)/test_main.cpp $(filter-out $(SRC_DIR)/main.cpp, $(SRCS))

# Map source files to object files (Preserve src and tests directories inside obj/)
OBJS        := $(SRCS:%.cpp=$(OBJ_DIR)/%.o)
TEST_OBJS   := $(TEST_SRCS:%.cpp=$(OBJ_DIR)/%.o)

# Generate dependency file paths
DEPS        := $(OBJS:.o=.d) $(TEST_OBJS:.o=.d)

# Default target
all: $(TARGET) ${TEST_TARGET}

# Debug mode setup
debug: CXXFLAGS := -Wall -Wextra -Isrc -g -DDEBUG
debug: all

# Link main executable
$(TARGET): $(OBJS)
	@echo "Linking application: $(BIN_DIR)/$@"
	@mkdir -p $(BIN_DIR)
	$(CXX) $(CXXFLAGS) $^ -o $(BIN_DIR)/$@

# Link test executable
$(TEST_TARGET): $(TEST_OBJS)
	@echo "Linking test suite: $(BIN_DIR)/$(TEST_TARGET)"
	$(CXX) $(CXXFLAGS) $^ -o $(BIN_DIR)/$(TEST_TARGET)
	@echo "Running tests..."
	./$(BIN_DIR)/$(TEST_TARGET)

# CORRECTED RULE: Matches obj/src/ or obj/tests/ explicitly
$(OBJ_DIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	@echo "Compiling: $< -> $@"
	$(CXX) $(CXXFLAGS) $(DEPFLAGS) -c $< -o $@

# Include generated dependency files (ignores errors on first build)
-include $(DEPS)

# Clean up all build artifacts
clean:
	@echo "Cleaning up..."
	rm -rf $(OBJ_DIR) $(BIN_DIR)

.PHONY: all debug test clean
