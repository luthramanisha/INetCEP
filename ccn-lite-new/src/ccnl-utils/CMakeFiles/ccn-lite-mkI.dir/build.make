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
include ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/depend.make

# Include the progress variables for this target.
include ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/progress.make

# Include the compile flags for this target's objects.
include ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/flags.make

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/flags.make
ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o: ccnl-utils/src/ccn-lite-mkI.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o   -c /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils/src/ccn-lite-mkI.c

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.i"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils/src/ccn-lite-mkI.c > CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.i

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.s"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils/src/ccn-lite-mkI.c -o CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.s

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.requires:

.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.requires

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.provides: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.requires
	$(MAKE) -f ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/build.make ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.provides.build
.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.provides

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.provides.build: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o


# Object files for target ccn-lite-mkI
ccn__lite__mkI_OBJECTS = \
"CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o"

# External object files for target ccn-lite-mkI
ccn__lite__mkI_EXTERNAL_OBJECTS =

bin/ccn-lite-mkI: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o
bin/ccn-lite-mkI: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/build.make
bin/ccn-lite-mkI: lib/libccnl-core.a
bin/ccn-lite-mkI: lib/libccnl-pkt.a
bin/ccn-lite-mkI: lib/libccnl-fwd.a
bin/ccn-lite-mkI: lib/libccnl-unix.a
bin/ccn-lite-mkI: lib/libccnl-nfn.a
bin/ccn-lite-mkI: ccnl-utils/libcommon.a
bin/ccn-lite-mkI: lib/libccnl-core.a
bin/ccn-lite-mkI: lib/libccnl-pkt.a
bin/ccn-lite-mkI: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/johannes/MA-Ali/ccn-lite/src/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable ../bin/ccn-lite-mkI"
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/ccn-lite-mkI.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/build: bin/ccn-lite-mkI

.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/build

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/requires: ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/src/ccn-lite-mkI.c.o.requires

.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/requires

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/clean:
	cd /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils && $(CMAKE_COMMAND) -P CMakeFiles/ccn-lite-mkI.dir/cmake_clean.cmake
.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/clean

ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/depend:
	cd /home/johannes/MA-Ali/ccn-lite/src && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils /home/johannes/MA-Ali/ccn-lite/src /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils /home/johannes/MA-Ali/ccn-lite/src/ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : ccnl-utils/CMakeFiles/ccn-lite-mkI.dir/depend

