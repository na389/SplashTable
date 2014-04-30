#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac
JFLAGS = -g
RM = del
default: SplashTable

B?=2

R?=1

S?=2

h?=1

inputfile?=inputfile

dumpfile?=dumpfile

probefile?=probefile

resultfile?=resultfile

#splash B R S h inputfile dumpfile < probefile > resultfile
splash: SplashTable.class
	java -jar splash.jar $(B) $(R) $(S) $(h) $(inputfile) $(dumpfile) $(probefile) $(resultfile)

jarfile: 
	jar -cfm splash.jar manifest *.class

SplashTable: SplashTable.java
	$(JCC) SplashTable.java KeyValue.java

clean:
	$(RM) *.class