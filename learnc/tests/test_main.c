#include "common.h"
#include "../src/linkedlist/linkedlist.h"

// --- Light Internal Testing Framework Engine ---
int tests_run = 0;
int tests_failed = 0;
int tests_total_run = 0;
int tests_total_failed = 0;

void reset_run_stats() {
    tests_run = tests_failed = 0;
}

void show_run_summary(char* test_suite) {
    tests_total_run += tests_run;
    tests_total_failed += tests_failed;

    printf("\n=== TEST SUITE %s ===\n", test_suite);
    printf("Executed = %-3d, Total = %-3d\n", tests_run, tests_total_run);
    printf("Passed   = %-3d, Total = %-3d\n", (tests_run - tests_failed), (tests_total_run-tests_total_failed) );
    printf("Failed   = %-3d, Total = %-3d\n", tests_failed, tests_total_failed);
    if (tests_failed > 0) {
        exit(1);
    }
    reset_run_stats();
}

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
    linked_list* node = add_node(&secret_num, sizeof(int));

    ASSERT_TRUE(node != NULL, "Node should be successfully allocated");
    ASSERT_TRUE(node->value != NULL, "Payload value pointer should not be NULL");
    ASSERT_TRUE(*(int*)(node->value) == 1337, "Extracted integer payload values should match");
    ASSERT_TRUE(node->next == NULL, "New individual node should default next pointer to NULL");

    traverse_free(node);
    return 0; // Success
}

int test_string_node_allocation() {
    char text[] = "Generic C Linked List";

    init_linked_list();
    linked_list* node = add_node(text, strlen(text) + 1);

    ASSERT_TRUE(node != NULL, "Node should be successfully allocated");
    ASSERT_TRUE(strcmp((char*)(node->value), "Generic C Linked List") == 0, "String contents should mirror exactly");

    traverse_free(node);
    return 0; // Success
}

int test_invalid_allocation_handling() {
    init_linked_list();
    linked_list* node = add_node(NULL, 10);
    ASSERT_TRUE(node != NULL, "Passing a NULL payload reference should create initial node");
    ASSERT_TRUE(node->value == NULL, "Passing a NULL payload reference should create initial node with NULL as value");
    traverse_free(node);

    return 0; // Success
}

int test_circular_integer_node_allocation() {
    int secret_num = 1337;

    init_linked_list();
    linked_list* node = add_node_circular(&secret_num, sizeof(int));

    ASSERT_TRUE(node != NULL, "Node should be successfully allocated");
    ASSERT_TRUE(node->value != NULL, "Payload value pointer should not be NULL");
    ASSERT_TRUE(*(int*)(node->value) == 1337, "Extracted integer payload values should match");
    ASSERT_TRUE(node->next != NULL, "New individual node should default next pointer to NOT NULL");

    traverse_free(node);
    return 0; // Success
}

void test_linked_list() {
    TEST_RUN(test_integer_node_allocation);
    TEST_RUN(test_string_node_allocation);
    TEST_RUN(test_invalid_allocation_handling);
    show_run_summary("test_linked_list");
}

void test_circular_linked_list() {
    TEST_RUN(test_circular_integer_node_allocation);
    show_run_summary("test_circular_linked_list");
}

// --- Main Test Suite Runner ---
int main() {
    printf("=== STARTING TEST SUITE ===\n");
    set_test_mode();

    test_linked_list();
    test_circular_linked_list();

    printf("=== COMPLETED TEST SUITE ===\n");
    return 0;
}
