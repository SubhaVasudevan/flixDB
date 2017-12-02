PROJPATH := ./
TEST_PACKAGE := com.karthik.test.
SRC := ${PROJPATH}src/
LIB := ${PROJPATH}lib/
CLASSPATH := ".:${LIB}*:${SRC}"
SUBDIR_ROOTS := src
DIRS := . $(shell find $(SUBDIR_ROOTS) -type d)
FILE_DELETION_PATTERNS := *.class
FILES_TO_DELETE := $(foreach DIR,$(DIRS),$(addprefix $(DIR)/,$(FILE_DELETION_PATTERNS)))
FILE_TO_COMPILE_PATTERNS := *.java
FILES_TO_COMPILE := $(foreach DIR,$(DIRS),$(addprefix $(DIR)/,$(FILE_TO_COMPILE_PATTERNS)))
SOURCES := $(shell find ${SRC} -name '*.java')

TESTSRC= ${TEST_PACKAGE}flixDBTests.DBStoreTest\
		 ${TEST_PACKAGE}flixDBClientTests.DBClientImplementationTest\

all:
	javac -cp ${CLASSPATH} ${SOURCES}

test:
	java -javaagent:"${LIB}jamm-0.3.2.jar" -cp ${CLASSPATH} org.junit.runner.JUnitCore ${TESTSRC}

clean:
	rm -rf $(FILES_TO_DELETE)
