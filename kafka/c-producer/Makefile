ALL: rdkafka_producer

CXXFLAGS=-Wc++11-extensions -std=c++17
CFLAGS=-Wall $(shell pkg-config --cflags glib-2.0 rdkafka rdkafka++)
LDLIBS=$(shell pkg-config --libs glib-2.0 rdkafka rdkafka++)
INC=-I/usr/local/include -I/Library/Developer/CommandLineTools/usr/lib/clang/15.0.0/include -I/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include -I/Library/Developer/CommandLineTools/usr/include -I/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/System/Library/Frameworks