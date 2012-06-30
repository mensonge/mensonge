PROJECT=LieLab
TARGETDIR=.
TARGET=userinterface.GraphicalUserInterface
TARGET_EXTRACTION=core.Extraction
TARGET_PLUGINS=core.plugins.PluginManager
ARGS=

SRCDIR=src
OBJDIR=bin
LIBDIR=lib

SRC=$(shell find $(SRCDIR) -name "*.java")
OBJ=$(patsubst $(SRCDIR)/%.java,$(OBJDIR)/%.class,$(SRC))

CLASSPATH=$(shell find $(LIBDIR) -name "*.jar" -printf "%p:")$(OBJDIR)
RUN_OPTIONS=-classpath $(CLASSPATH) $(TARGET) $(ARGS)
RUN_EXTRACTION=-classpath $(CLASSPATH) $(TARGET_EXTRACTION) $(ARGS)
RUN_PLUGINS=-classpath $(CLASSPATH) $(TARGET_PLUGINS) $(ARGS)

WARN=-Xlint:all
COMPILE_OPTIONS=$(WARN) -d $(OBJDIR) -sourcepath $(SRCDIR) -classpath $(CLASSPATH)

all: compile

run: compile
	java $(RUN_OPTIONS) 

run_extraction: compile
	java $(RUN_EXTRACTION)

run_plugins: compile
	java $(RUN_PLUGINS)

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

jar: manifest
	(cd $(OBJDIR) && jar cvmf META-INF/MANIFEST.MF ../$(PROJECT).jar $(TARGETDIR)/* )
