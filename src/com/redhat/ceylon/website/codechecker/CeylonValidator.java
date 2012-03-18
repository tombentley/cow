package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

import com.redhat.ceylon.compiler.java.tools.CeyloncFileManager;
import com.redhat.ceylon.compiler.java.tools.CeyloncTaskImpl;
import com.redhat.ceylon.compiler.java.tools.CeyloncTool;
import com.sun.tools.javac.zip.ZipFileIndex;

/**
 * A {@link Validator} for Ceylon source code.
 * @author tom
 */
public class CeylonValidator implements Validator {

    private final PrintStream out;
    
    public CeylonValidator(PrintStream out) {
        this.out = out;
    }
    
    @Override
    public boolean isValid(File file, int startLine, String source, String check) {
        List<Error> errors;
        File tmpSrc = dumpSourceToFile(source);
        try {
            File out = tmpDir();
            try {
                // TODO support values for 'check': parse, type and compile
                // if no value then assume compile
                errors = compileFile(tmpSrc, out);
            } finally {
                delete(out);
            }
        } finally {
            delete(tmpSrc);
        }
    
        if (!errors.isEmpty()) {
            System.out.println("ERROR: Code starting on line " + startLine + " of "+ file + ":");
            System.out.println(source);
            for (Error error : errors) {
                error.report(startLine);
            }
            System.out.println();
        }
        
        return errors.isEmpty();
    }

    private File tmpDir() {
        try {
            File out = File.createTempFile("sitetool", ".out.d");
            out.delete();
            out.mkdirs();
            return out;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    private File dumpSourceToFile(String source) {
        try {
            File file = File.createTempFile("sitetool", ".ceylon");
            file.deleteOnExit();
            FileWriter writer = new FileWriter(file);
            try {
                writer.write(source);
            } finally {
                writer.close();
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    protected CeyloncTool makeCompiler(){
        try {
            return new CeyloncTool();
        } catch (VerifyError e) {
            System.err.println("ERROR: Cannot run tests! Did you maybe forget to configure the -Xbootclasspath/p: parameter?");
            throw e;
        }
    }

    protected CeyloncFileManager makeFileManager(CeyloncTool compiler, 
            DiagnosticListener<? super FileObject> diagnosticListener ){
        return (CeyloncFileManager)compiler.getStandardFileManager(diagnosticListener, null, null);
    }
    
    protected CeyloncTaskImpl getCompilerTask(List<String> initialOptions, 
            DiagnosticListener<? super FileObject> diagnosticListener,
            File destDir,
            File sourcePaths){
        // make sure we get a fresh jar cache for each compiler run
        ZipFileIndex.clearCache();
        java.util.List<File> sourceFiles = Collections.singletonList(sourcePaths);
        
        CeyloncTool runCompiler = makeCompiler();
        CeyloncFileManager runFileManager = makeFileManager(runCompiler, diagnosticListener);

        // make sure the destination repo exists
        destDir.mkdirs();

        List<String> options = new LinkedList<String>();
        options.addAll(initialOptions);
        options.addAll(Arrays.asList("-src", sourcePaths.getParent()));
        Iterable<? extends JavaFileObject> compilationUnits1 =
                runFileManager.getJavaFileObjectsFromFiles(sourceFiles);
        return (CeyloncTaskImpl) runCompiler.getTask(null, runFileManager, diagnosticListener, 
                options, null, compilationUnits1);
    }
    
    private List<Error> compileFile(File file, File out) {
     // make a compiler task
        // FIXME: runFileManager.setSourcePath(dir);
        
        final List<String> defaultOptions = Arrays.asList(
                new String[]{"-out", out.getAbsolutePath()/*, "-rep", destDir*/});
 
        final List<Error> errors = new LinkedList<Error>();
        
        @SuppressWarnings("unchecked")
        DiagnosticListener<? super FileObject> diagnosticListener = new DiagnosticListener() {
            @Override
            public void report(Diagnostic diagnostic) {
                switch (diagnostic.getKind()) {
                case ERROR:
                    errors.add(new Error(diagnostic.getLineNumber(),
                    diagnostic.getColumnNumber(),
                    diagnostic.getMessage(null)));
                    break;
                default:
                }
            }
        };
        
        CeyloncTaskImpl task = getCompilerTask(
                defaultOptions, diagnosticListener, out, file);

        // now compile it all the way
        Boolean success = task.call();
        if (!success && errors.size() == 0) {
            errors.add(new Error(0, 0, "Unexplained error"));
        }
        
        return errors;
    }
    
    class Error {
        final long line;
        final long column;
        final String message;
        public Error(long line, long column, String message) {
            super();
            this.line = line;
            this.column = column;
            this.message = message;
        }
        public void report(int startLine) {
            out.println(line + ":" + column + ":" + message);
        }
    }

    
}
