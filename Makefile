#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac
JFLAGS = -g
RM = del
default: SplashTable

SplashTable: SplashTable.java
	$(JCC) SplashTable.java KeyValue.java

clean:
	$(RM) *.class