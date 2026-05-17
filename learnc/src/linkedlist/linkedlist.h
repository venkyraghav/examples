// linkedlist.h
#ifndef LINKED_LIST_H
#define LINKED_LIST_H

typedef struct linked_list {
    void* value;
    struct linked_list* next;
} linked_list;


typedef void (*print_value)(void*);

int fn_linked_list();

void init_linked_list();
void print_int_value(void* value);
void print_linked_list(struct linked_list *intList);
void free_list(struct linked_list *intList);
struct linked_list* create_node(void* value, size_t size);

#endif
