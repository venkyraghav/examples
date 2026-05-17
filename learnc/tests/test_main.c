#include "common.h"
#include "../src/linkedlist/linkedlist.h"

// --- Light Internal Testing Framework Engine ---
int tests_run = 0;
int tests_failed = 0;


bool test_mode = false;
bool is_test_mode() {
    return test_mode;
}

void set_test_mode() {
    test_mode = true;
}

#define TEST_RUN(test_func) do { \
    printf("Running %s... ", #test_func); \
    int status = test_func(); \
    tests_run++; \
    if (status == 0) { \
        printf("\033[0;32mPASSED\033[0m\n"); \
    } else { \
        tests_failed++; \
    } \
} while(0)

#define ASSERT_TRUE(expr, msg) do { \
    if (!(expr)) { \
        printf("\033[0;31mFAILED\033[0m\n  Line %d: %s\n", __LINE__, msg); \
        return 1; \
    } \
} while(0)

// --- Individual Unit Tests ---

int test_integer_node_allocation() {
    int secret_num = 1337;

    init_linked_list();
    linked_list* node = create_node(&secret_num, sizeof(int));

    ASSERT_TRUE(node != NULL, "Node should be successfully allocated");
    ASSERT_TRUE(node->value != NULL, "Payload value pointer should not be NULL");
    ASSERT_TRUE(*(int*)(node->value) == 1337, "Extracted integer payload values should match");
    ASSERT_TRUE(node->next == NULL, "New individual node should default next pointer to NULL");

    free_list(node);
    return 0; // Success
}

int test_string_node_allocation() {
    char text[] = "Generic C Linked List";

    init_linked_list();
    linked_list* node = create_node(text, strlen(text) + 1);

    ASSERT_TRUE(node != NULL, "Node should be successfully allocated");
    ASSERT_TRUE(strcmp((char*)(node->value), "Generic C Linked List") == 0, "String contents should mirror exactly");

    free_list(node);
    return 0; // Success
}

int test_invalid_allocation_handling() {
    init_linked_list();
    linked_list* node = create_node(NULL, 10);
    ASSERT_TRUE(node != NULL, "Passing a NULL payload reference should create initial node");
    ASSERT_TRUE(node->value == NULL, "Passing a NULL payload reference should create initial node with NULL as value");

    return 0; // Success
}

// --- Main Test Suite Runner ---
int main() {
    printf("=== STARTING LINKED LIST TEST SUITE ===\n\n");
    set_test_mode();

    TEST_RUN(test_integer_node_allocation);
    TEST_RUN(test_string_node_allocation);
    TEST_RUN(test_invalid_allocation_handling);

    printf("\n=== TEST SUMMARY ===\n");
    printf("Total Executed: %d\n", tests_run);
    printf("Passed:         %d\n", tests_run - tests_failed);
    printf("Failed:         %d\n", tests_failed);

    return (tests_failed > 0) ? 1 : 0;
}
