
#include "../common.h"
#include "arraylist.h"

#define GROW_SIZE 10

typedef struct array_list_int {
    size_t grow_size;
    size_t current_size;
    size_t array_size;
    int* start;
} array_list_int;

struct array_list_int* new_array_list_int() {
    struct array_list_int* list = malloc(sizeof(array_list_int));
    list->grow_size = GROW_SIZE;
    list->array_size = 0;
    list->current_size = 0;
    list->start = NULL;
    grow_array_int(list, sizeof(int));
    return list;
}

void grow_array_int(struct array_list_int* list, size_t element_size) {
    printf("grow_array_int ...\n");
    grow_by_size_array_int(list, element_size);
}

void grow_by_size_array_int(struct array_list_int* list, size_t element_size) {
    printf("grow_by_size_array_int ...\n");
    int* tmp = malloc(list->array_size+list->grow_size*element_size);
    printf("grow_by_size_array_int %p\n", tmp);
    if (list->start != NULL) {
        memcpy(tmp, list->start, list->array_size);
    }
    int* free_this = list->start;
    list->array_size += list->grow_size;
    list->start = tmp;
    printf("grow_by_size_array_int %p\n", list->start);
    free_array_int(free_this);
}

void free_array_int(int* elements) {
    if (elements == NULL) {
        return;
    }
    printf("not implemented...");
}

void set_grow_size_int(struct array_list_int* list, size_t size) {
    list->grow_size = size;
}

void compact_array_int(struct array_list_int* list) {
    printf("not implemented...");
}

void add_element_array_int(struct array_list_int* list, int* value) {
    printf("add_element_array_int ...\n");
    if (list->current_size == list->array_size) {
        printf("list->current_size == list->array_size ...\n");
        grow_array_int(list, sizeof(value));
    }
    printf("Before list->start %p %d %d\n", list->start, list->current_size, *value);
    *(list->start + list->current_size) = *value;
    printf("After list->start\n");
    list->current_size++;
}

void insert_element_array_int(struct array_list_int* list, void* value, short index) {
    printf("not implemented...");
}

void delete_element_array_int(struct array_list_int* list, short index) {
    printf("not implemented...");
}

int fn_array_list_int() {
    printf("Starting fn_array_list ...\n");
    struct array_list_int* array = new_array_list_int();
    printf("fn_array_list_int before \n");
    printf("fn_array_list_int %p\n", array->start);
    printf("Creating nodes fn_array_list ...\n");
    for (int i = 0;i < 20;i++) {
        int* value = malloc(sizeof(i));
        *value = i;
        add_element_array_int(array, value);
    }

    printf("Exiting fn_array_list ...\n");

    return 0;
}
