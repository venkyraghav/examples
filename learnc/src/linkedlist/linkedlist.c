
#include "../common.h"
#include "linkedlist.h"

struct linked_list* head = NULL;
struct linked_list* tail = NULL;

traverse_helper print_int_fn = traverse_int_printer;
traverse_helper free_fn = traverse_free;

void traverse_int_printer(struct linked_list* node) {
    if (!is_test_mode()) printf("traverse_int_printer: Value is %d\n", *(int*)(node->value));
}

void traverse_free(struct linked_list* node) {
    traverse_int_printer(node);
    if (!is_test_mode()) printf("traverse_free: cleaning up ptr %p\n", (void*)node);
    free(node);
}

void init_linked_list() {
    head = tail = NULL;
}

struct linked_list* get_head() {
    return head;
}

struct linked_list* get_tail() {
    return tail;
}

void traverse(struct linked_list *node, traverse_helper helper) {
    if (node == NULL)
        return;
    if (helper != NULL) {
        helper(node);
    }
    traverse(node->next, helper);
}

struct linked_list* add_node(void* value, size_t size) {
    struct linked_list* node = malloc(sizeof(struct linked_list));
    node->next = NULL;
    if (value != NULL) {
        node->value = malloc(size);
        memcpy(node->value, value, size);
    }

    if (head == NULL) { // if no head, assign head, tail
        if (!is_test_mode()) printf("add_node: head is NULL\n");
        head = tail = node;
    } else { // assign node to tail's next
        tail->next = node;
    }

    tail = node; // make new tail

    return tail;
}

int fn_linked_list() {
    printf("Starting fn_linked_list ...\n");
    init_linked_list();
    printf("Creating nodes fn_linked_list ...\n");
    for (int i = 0;i < 10;i++) {
        add_node(&i, sizeof(int));
    }
    printf("Printing nodes fn_linked_list ...\n");
    traverse(head, print_int_fn);
    printf("Freeing nodes fn_linked_list ...\n");
    traverse(head, free_fn);
    printf("Exiting fn_linked_list ...\n");

    return 0;
}

void traverse_circular(struct linked_list *node, traverse_helper helper) {
    if (head == NULL)
        return;
    if (node->next == head) {
        if (helper != NULL) {
            helper(node);
        }
        return;
    } else {
        traverse_circular(node->next, helper);
        if (helper != NULL) {
            helper(node);
        }
    }
}

struct linked_list* add_node_circular(void* value, size_t size) {
    struct linked_list* node = malloc(sizeof(struct linked_list));
    if (value != NULL) {
        node->value = malloc(size);
        memcpy(node->value, value, size);
    }

    if (head == NULL) { // if no head, assign head, tail
        if (!is_test_mode()) printf("add_node: head is NULL\n");
        head = tail = node;
    } else { // assign node to tail's next
        tail->next = node;
    }
    node->next = head;
    tail = node; // make new tail

    return tail;
}

int fn_linked_list_circular() {
    printf("Starting fn_circular_linked_list ...\n");
    init_linked_list();
    printf("Creating nodes fn_circular_linked_list ...\n");
    for (int i = 0;i < 10;i++) {
        add_node_circular(&i, sizeof(int));
    }
    printf("Printing nodes fn_circular_linked_list ...\n");
    traverse_circular(head, print_int_fn);
    printf("Freeing nodes fn_circular_linked_list ...\n");
    traverse_circular(head, free_fn);
    printf("Exiting fn_circular_linked_list ...\n");

    return 0;
}
