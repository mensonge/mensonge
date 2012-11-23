PROJECT=CoefficientsCepstraux
TARGETDIR=.
TARGET=coeffCepstraux.core.CoefficientsCepstraux
ARGS=

SRCDIR=src
OBJDIR=bin
LIBDIR=../../lib

SRC=$(shell find $(SRCDIR) -name "*.java")
OBJ=$(patsubst $(SRCDIR)/%.java,$(OBJDIR)/%.class,$(SRC))

CLASSPATH=$(shell find $(LIBDIR) -name "*.jar" -printf "%p:")$(OBJDIR)
RUN_OPTIONS=-classpath $(CLASSPATH) $(TARGET) $(ARGS)

WARN=-Xlint:all
COMPILE_OPTIONS=$(WARN) -d $(OBJDIR) -sourcepath $(SRCDIR) -classpath $(CLASSPATH)

all: compile

run: compile
	java $(RUN_OPTIONS) 

compile: $(OBJDIR) $(OBJ)

$(OBJDIR)/%.class: $(SRCDIR)/%.java
	javac $< $(COMPILE_OPTIONS)

$(OBJDIR):
	@test -d $(OBJDIR) || mkdir $(OBJDIR)

clean:
	rm -fr $(OBJDIR)
	rm  -f $(PROJECT).jar

manifest:
	mkdir -p $(OBJDIR)/META-INF && echo "Main-Class: $(TARGET)" > $(OBJDIR)/META-INF/MANIFEST.MF

jar: compile manifest
	(cd $(OBJDIR) && jar cvmf META-INF/MANIFEST.MF ../$(PROJECT).jar $(TARGETDIR)/* )
