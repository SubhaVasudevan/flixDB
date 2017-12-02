# Summary

The name I gave this key-value store is FlixDB. It is an in-memory store accessible by multiple clients concurrently. FlixDB is written in Java. When I started the project, the goal was to understand how key-value systems are designed and implemented from ground-up. 

# Requirements

1. Java >= 1.8 (Only Oracle JVMS has been tested)
2. GNU Bash 3.2 The Makefile and the shell scripts used in this project only works in
    Bash. If you use other shells, you may have to change the scripts in $FLIXDB_HOME/bin
3. Disconnect your VPN services if you are testing on your local machine. Depending on
    your VPN provider, the client to server connection may get blocked and eventually
    timeout.
4. Read this document till the end to understand the limitations of the system before you
    attempt to run this.

# Required Configuration Files

conf/flixDB.yaml: Main FlixDB configuration file in the $FLIXDB_HOME/conf
directory. The server configuration supports the following parameters,

1. max_concurrent_client_connections – Maximum number of concurrent
    client connections supported by the server.
2. max_keyspace_memory – Maximum memory size in **bytes** for the in-memory
    keyspace. This includes memory used by all the data structures used to maintain the
    keyspace. Setting it to zero will use all the available JVM memory.
3. server_port – FlixDB server will listen on this port.


# Getting Started

This short guide will walk you through getting a basic single machine FlixDB server/client up
and running, and demonstrate some simple reads and writes.

First, we'll unpack the archive:

$ tar -zxvf flixDB.tar.gz
$ cd flixDB
$ make

The make command will build both the server and the client. After that we can start the FlixDB
server. You need to be **_in the filxDB/bin directory_** as $FLIXDB_HOME is set relative to that.
The server can be stopped with ctrl-C. By default, the server uses port 14567 for client
connections. It can be changed in the conf/flixDB.yaml file as mentioned above.

$ cd bin
$ ./flixDBServer

The flixDBClient is also part of the same project. It can be run from a different terminal after
running make from $FLIXDB_HOME. Here is an example session,

$ cd bin
$ ./flixDBClient <server_ip>
> SET foo 3
> bar
< OK
> GET foo
< VALUE 3
< bar
> SET foo2 4
> bar
< OK
> STREAM
< KEY foo2 VALUE 0
< bar
< KEY foo VALUE 0
< bar
< KEY foo1 VALUE 0
< bar
> DELETE foo
< OK
> STREAM
< KEY foo2 VALUE 0
< bar
< KEY foo1 VALUE 0
< bar
> GET foo bar
< ERROR

Process finished with exit code 0

# Design

Multiple FlixDB clients will be accessing the single-server FlixDB key-value store using the given
protocol format over the network through sockets.

Here is a diagram of all the major parts of the system and how they interact with each other.

The core data structure on the server side is a _ConcurrentHashMap_ provided by Java. It is
thread-safe and supports full concurrency of updates and retrievals.

# FlixDB Server

On the server side, there is a _ThreadPool_ with a number of threads that is equal to the max
number of client connections supported. This _ThreadPool_ is the workhorse of the system which
uses a job queue to service clients concurrently. When a client request comes in, the
_SocketServer_ would create a _ClientHandler_ and register it with the _ThreadPool_. As the
commands come in from the clients, it will be serviced by the _ThreadPool_ using the
_ClientHandler_.

The client and the server communicate using the serialized _DBRequest_ and _DBResponse_ objects.

There were a few design choices that I had to make based on the information provided in the
problem statement.

- DELETE command will return OK if the key does not exist in the store. This is because
    the key could have been evicted due to memory pressure on the server side. We
    achieve the end goal of removing the key from the server in both cases.
- STREAM command will return results in the same format as GET command.
- Running STREAM command two times in a row will not result in reversal of the LRU
    order. I would like to think STREAM as a way to get the current state of the DBStore and
    not to alter it.
- Invalid input or any kind of input validation is done by the FlixDB client. This is to avoid
    bombarding FlixDB server with spurious input.

# Memory Management:

The max_keyspace_memory configuration directive is used in order to configure FlixDB to
use a specified amount of memory for the key-value store. It is possible to set the configuration
directive using the flixDB.conf file

Setting max_keyspace_memory to zero results in the store all the available memory
allocated for the JVM.

When the specified amount of memory is reached, FlixDB evict keys by trying to remove the
less recently used (LRU) keys first, in order to make space for the new data added. I choose LRU
as I expect a power-law distribution in the popularity of requests coming to services like Netflix,
that is, you expect that a subset of elements will be accessed far more often than the rest.

It is important to understand that the eviction process works like this:

- FlixDBClient runs a new SET command, resulting in more data added.
- FlixDBServer checks the memory usage, and if it is greater than the
    max_keyspace_memory limit, it evicts keys according to the policy.
- The new SET command is executed, and so forth.

So, we continuously cross the boundaries of the memory limit, by going over it, and then by
evicting keys to return back under the limits.

If a SET command results in a lot of memory being used for some time the memory limit can
be surpassed by a noticeable amount.

The memory allocation and de-allocation events and stats are displayed on the server side
console.

# Notes and Assumptions:

1. Line break/Carriage return characters such as /r/n, /n, /r are implicit. In other words, if
    you specify “\r\n” as text in the SET command, it will be treated as a text and not as
    special characters. (ex) “SET foo 3\r\nbar” will fail. But “SET foo 3<line break of OSX or
    Linux>bar will succeed.
2. Need to improve unit test coverage.
3. The build system of choice is make for its simplicity and availability across platforms.
    The goal is not to spend more than 5 minutes writing build script. Classes will compile to
    the same folder. Need to use gradle for build and deployment.
5. The external packages/libraries necessary for the FlixDB are present in the $FLIX_DB/lib
    folder for simplicity. Need to use gradle for build and deployment.
6. Unit tests can be run using make test. In the real world, I would again use gradle
    for setting up and running unit tests.
7. Logger is not being used. Need to fix that as well.

