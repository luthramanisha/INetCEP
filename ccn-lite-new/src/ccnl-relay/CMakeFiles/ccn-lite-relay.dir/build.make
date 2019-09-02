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
include ccnl-relay/CMakeFiles/ccn-lite-relay.dir/depend.make

# Include the progress variables for this target.
include ccnl-relay/CMakeFiles/ccn-lite-relay.dir/progress.make

# Include the compile flags for this target's objects.
include ccnl-relay/CMakeFiles/ccn-lite-relay.dir/flags.make

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/flags.make
ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o: ccnl-relay/ccn-lite-relay.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o   -c /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay/ccn-lite-relay.c

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.i"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay/ccn-lite-relay.c > CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.i

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.s"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay/ccn-lite-relay.c -o CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.s

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.requires:

.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.requires

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.provides: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.requires
	$(MAKE) -f ccnl-relay/CMakeFiles/ccn-lite-relay.dir/build.make ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.provides.build
.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.provides

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.provides.build: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o


# Object files for target ccn-lite-relay
ccn__lite__relay_OBJECTS = \
"CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o"

# External object files for target ccn-lite-relay
ccn__lite__relay_EXTERNAL_OBJECTS =

bin/ccn-lite-relay: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o
bin/ccn-lite-relay: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/build.make
bin/ccn-lite-relay: lib/libccnl-core.a
bin/ccn-lite-relay: lib/libccnl-pkt.a
bin/ccn-lite-relay: lib/libccnl-fwd.a
bin/ccn-lite-relay: lib/libccnl-unix.a
bin/ccn-lite-relay: lib/libccnl-nfn.a
bin/ccn-lite-relay: lib/libccnl-core.a
bin/ccn-lite-relay: lib/libccnl-pkt.a
bin/ccn-lite-relay: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable ../bin/ccn-lite-relay"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/ccn-lite-relay.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
ccnl-relay/CMakeFiles/ccn-lite-relay.dir/build: bin/ccn-lite-relay

.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/build

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/requires: ccnl-relay/CMakeFiles/ccn-lite-relay.dir/ccn-lite-relay.c.o.requires

.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/requires

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/clean:
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay && $(CMAKE_COMMAND) -P CMakeFiles/ccn-lite-relay.dir/cmake_clean.cmake
.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/clean

ccnl-relay/CMakeFiles/ccn-lite-relay.dir/depend:
	cd /home/johannes/MA-Ali/ccn-lite/src && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay /home/johannes/MA-Ali/ccn-lite/src/ccnl-relay/CMakeFiles/ccn-lite-relay.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : ccnl-relay/CMakeFiles/ccn-lite-relay.dir/depend

