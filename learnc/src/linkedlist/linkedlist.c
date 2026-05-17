
#include "../common.h"
#include "linkedlist.h"

struct linked_list* head = NULL;
struct linked_list* tail = NULL;
print_value printer = print_int_value;

void init_linked_list() {
    head = tail = NULL;
}

struct linked_list* get_head() {
    return head;
}

struct linked_list* get_tail() {
    return tail;
}

void print_int_value(void* value) {
    if (!is_test_mode()) printf("Value is %d\n", *(int*)(value));
}

void print_linked_list(struct linked_list *node) {
    if (!is_test_mode()) printf("print_linked_list\n");
    if (node == NULL)
        return;
    if (printer != NULL) {
        printer(node->value);
    }
    print_linked_list(node->next);
}

void free_list(struct linked_list *node) {
    if (!is_test_mode()) printf("free_list\n");
    if (node == NULL)
        return;
    free_list(node->next);
    if (printer != NULL) {
        printer(node->value);
    }
    if (!is_test_mode()) printf("Cleaning up ptr %p\n", (void*)node->next);
    free(node);
}

struct linked_list* create_node(void* value, size_t size) {
    if (!is_test_mode()) printf("create_node\n");
    struct linked_list* node = malloc(sizeof(struct linked_list));
    node->next = NULL;
    if (value != NULL) {
        node->value = malloc(size);
        memcpy(node->value, value, size);
        if (!is_test_mode()) {
            printer(node->value);
        }
    }

    if (head == NULL) { // if no head, assign head, tail
        if (!is_test_mode()) printf("create_node: head is NULL\n");
        head = tail = node;
    } else { // assign node to tail's next
        tail->next = node;
    }

    tail = node; // make new tail

    return tail;
}

int fn_linked_list() {
    init_linked_list();
    for (int i = 0;i < 10;i++) {
        create_node(&i, sizeof(int));
    }
    print_linked_list(head);
    free_list(head);

    return 0;
}
