// linkedlist.h
#ifndef LINKED_LIST_H
#define LINKED_LIST_H

typedef struct linked_list {
    void* value;
    struct linked_list* next;
} linked_list;

typedef void (*traverse_helper)(struct linked_list*);

void traverse_int_printer(struct linked_list* node);
void traverse_free(struct linked_list* node);

void init_linked_list();

int fn_linked_list();
void traverse(struct linked_list* node, traverse_helper helper);
struct linked_list* add_node(void* value, size_t size);

int fn_linked_list_circular();
void traverse_circular(struct linked_list* node, traverse_helper helper);
struct linked_list* add_node_circular(void* value, size_t size);

#endif
