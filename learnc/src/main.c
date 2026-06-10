
#include "common.h"
#include "linkedlist/linkedlist.h"
#include "arraylist/arraylist.h"

bool test_mode = false;
bool is_test_mode() {
    return test_mode;
}

void set_test_mode() {
    test_mode = true;
}

int main() {
    // return fn_linked_list();
    // return fn_linked_list_circular();
    return fn_array_list_int();
}
