# Building CCN-lite

## Prerequisites

CCN-lite requires OpenSSL. Use the following to install it:
* Ubuntu: `sudo apt-get install libssl-dev`
* OS X: `brew install openssl`

## Build

1.  Clone the repository:
    ```bash
    git clone https://github.com/cn-uofbasel/ccn-lite
    ```
    Or clone the ccn-lite folder from this repository.

2.  Set environment variable `$CCNL_HOME` and add the binary folder of CCN-lite to your `$PATH`:
    Default:
    ```bash
    export CCNL_HOME="`pwd`/ccn-lite"
    export PATH=$PATH:"$CCNL_HOME/bin"
    ```
    Customized to suit our development environment and VM:
    ```bash
    CCNL_HOME="/home/veno/Thesis/ccn-lite"
    JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
    OMNETPP="/home/veno/Thesis/omnetpp"
    export CCNL_HOME
    export JAVA_HOME
    export PATH=$PATH:"$CCNL_HOME/bin":"$JAVA_HOME/bin":"$OMNETPP/omnetpp-4.5/bin"
    export TCL_LIBRARY=/usr/share/tcltk/tcl8.6
    ```
   * /home/veno/ can be substituted with your relevant home directory in case the provided VM is not used.

    To make these variables permanent, add them to your shell's `.rc` file, e.g. `~/.bashrc`.

3.  Build CCN-lite using `make`:
    ```bash
    cd $CCNL_HOME/src
    make clean all
    ```

# Building NFN-Scala

## Prerequisites

* Get the customized nfn-scala folder from the working repository.
* Place it in your preferred directory. E.g. /home/project/nfn-scala/
* Make sure that SBT compiler is available on your machine.

## Build

1.  Navigate to the path where nfn-scala folder is placed (example):
    
    ```bash
    cd /home/project/nfn-scala/
    ```

2.  This directory contains the build.sbt file. Use this file to compile your sources:

    ```bash
    sbt compile
    ```

3.  Once the nfn-scala code compiles successfully, produce a JAR file for the Compute Server using:

    ```bash
    sbt assembly
    ```

    Once your assembly file (.JAR) has been created, it will be placed in the ../nfn-scala/target/scala-2.10/ folder. Use the .jar file to start the compute server.


* This completes the build procedures for CCN-Lite and NFN-Scala.



