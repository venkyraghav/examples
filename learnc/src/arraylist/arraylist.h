// arraylist.h
#ifndef ARRAY_LIST_H
#define ARRAY_LIST_H

int fn_array_list_int();
struct array_list_int* new_array_list_int();
void grow_array_int(struct array_list_int* list, size_t element_size);
void grow_by_size_array_int(struct array_list_int* list, size_t element_size);
void free_array_int(int* elements);
void set_grow_size_int(struct array_list_int* list, size_t size);
void compact_array_int(struct array_list_int* list);
void add_element_array_int(struct array_list_int* list, int* value);
void insert_element_array_int(struct array_list_int* list, void* value, short index);
void delete_element_array_int(struct array_list_int* list, short index);

#endif
