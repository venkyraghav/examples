
#include "common.h"
#include "linkedlist/linkedlist.h"


bool test_mode = false;
bool is_test_mode() {
    return test_mode;
}

void set_test_mode() {
    test_mode = true;
}

int main() {
    printf("main\n");
    return fn_linked_list();
}
