#include <iostream>
#include "initialtest.h"

InitialTest::InitialTest(/* args */)
{
    std::cout << "InitialTest Constructor" << std::endl;
}

InitialTest::~InitialTest()
{
    std::cout << "InitialTest Destructor" << std::endl;
}

void InitialTest::runTest()
{
    // Here's some math/physics values that we copy-pasted from elsewhere
    double pi { 3.14159 };
    double gravity { 9.8 };
    double phi { 1.61803 };

    std::cout << pi << '\n';  // pi is used
    std::cout << phi << '\n'; // phi is used
    std::cout << gravity << '\n'; // gravity is used
}
