# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.9

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/local/bin/cmake

# The command to remove a file.
RM = /usr/local/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/johannes/MA-Ali/ccn-lite/src

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/johannes/MA-Ali/ccn-lite/src

# Include any dependencies generated for this target.
include ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/depend.make

# Include the progress variables for this target.
include ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/progress.make

# Include the compile flags for this target's objects.
include ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/flags.make

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/flags.make
ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o: ccnl-core/test/ccnl_core_test_uri_to_prefix.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-core && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o   -c /home/johannes/MA-Ali/ccn-lite/src/ccnl-core/test/ccnl_core_test_uri_to_prefix.c

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.i"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-core && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/johannes/MA-Ali/ccn-lite/src/ccnl-core/test/ccnl_core_test_uri_to_prefix.c > CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.i

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.s"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-core && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/johannes/MA-Ali/ccn-lite/src/ccnl-core/test/ccnl_core_test_uri_to_prefix.c -o CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.s

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.requires:

.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.requires

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.provides: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.requires
	$(MAKE) -f ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/build.make ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.provides.build
.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.provides

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.provides.build: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o


# Object files for target ccnl_core_test_uri_to_prefix
ccnl_core_test_uri_to_prefix_OBJECTS = \
"CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o"

# External object files for target ccnl_core_test_uri_to_prefix
ccnl_core_test_uri_to_prefix_EXTERNAL_OBJECTS =

ccnl-core/ccnl_core_test_uri_to_prefix: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o
ccnl-core/ccnl_core_test_uri_to_prefix: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/build.make
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-core.a
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-pkt.a
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-fwd.a
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-nfn.a
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-unix.a
ccnl-core/ccnl_core_test_uri_to_prefix: /usr/lib/x86_64-linux-gnu/libssl.so
ccnl-core/ccnl_core_test_uri_to_prefix: /usr/lib/x86_64-linux-gnu/libcrypto.so
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-core.a
ccnl-core/ccnl_core_test_uri_to_prefix: lib/libccnl-pkt.a
ccnl-core/ccnl_core_test_uri_to_prefix: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable ccnl_core_test_uri_to_prefix"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-core && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/ccnl_core_test_uri_to_prefix.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/build: ccnl-core/ccnl_core_test_uri_to_prefix

.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/build

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/requires: ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/test/ccnl_core_test_uri_to_prefix.c.o.requires

.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/requires

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/clean:
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-core && $(CMAKE_COMMAND) -P CMakeFiles/ccnl_core_test_uri_to_prefix.dir/cmake_clean.cmake
.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/clean

ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/depend:
	cd /home/johannes/MA-Ali/ccn-lite/src && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-core /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-core /home/johannes/MA-Ali/ccn-lite/src/ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : ccnl-core/CMakeFiles/ccnl_core_test_uri_to_prefix.dir/depend

