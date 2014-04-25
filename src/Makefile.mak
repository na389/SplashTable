#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac
JFLAGS = -g

default: SplashHashMap.class


#B?=

#R?=

#S?=

#h?=

#inputfile?=

#dumpfile?=

#probefile?=

#resultfile?=

#splash B R S h inputfile dumpfile < probefile > resultfile
#splash: java SplashHashMap.class $(B) $(R) $(S) $(h) $(inputfile) $(dumpfile) $(probefile) $(resultfile)

SplashHashMap.class: SplashHashMap.java
	$(JCC) SplashHashMap.java

clean:
	$(RM)*.class