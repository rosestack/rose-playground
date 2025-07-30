package io.github.rosestack.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.*;

/**
 * Processes {@link AutoService} annotations and generates the service provider configuration files
 * described in {@link java.util.ServiceLoader}.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://github.com/kohsuke/metainf-services/">metainf-services</a>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({"debug", "verify"})
@SupportedAnnotationTypes("io.github.rosestack.processor.AutoService")
public class AutoServiceProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * <ol>
     *   <li>For each class annotated with {@link AutoService}
     *       <ul>
     *         <li>Verify the {@link AutoService} interface value is correct
     *         <li>Categorize the class by its service interface
     *       </ul>
     *   <li>For each {@link AutoService} interface
     *       <ul>
     *         <li>Create a file named {@code META-INF/services/<interface>}
     *         <li>For each {@link AutoService} annotated class for this interface
     *             <ul>
     *               <li>Create an entry in the file
     *             </ul>
     *       </ul>
     * </ol>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Map<String, Set<String>> services = new HashMap<>();
        Elements elements = processingEnv.getElementUtils();

        // discover services from the current compilation sources
        for (Element e : roundEnv.getElementsAnnotatedWith(AutoService.class)) {
            AutoService a = e.getAnnotation(AutoService.class);
            if (a == null) {
                continue; // input is malformed, ignore
            }
            if (!e.getKind().isClass() && !e.getKind().isInterface()) {
                continue; // ditto
            }
            TypeElement type = (TypeElement) e;
            Collection<TypeElement> contracts = getContracts(type, a);
            if (contracts.isEmpty()) {
                continue; // error should have already been reported
            }

            for (TypeElement contract : contracts) {
                String cn = elements.getBinaryName(contract).toString();
                Set<String> v = services.computeIfAbsent(cn, k -> new TreeSet<>());
                v.add(elements.getBinaryName(type).toString());
            }
        }

        // also load up any existing values, since this compilation may be partial
        Filer filer = processingEnv.getFiler();
        for (Map.Entry<String, Set<String>> e : services.entrySet()) {
            try {
                String contract = e.getKey();
                FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + contract);
                BufferedReader r =
                        new BufferedReader(new InputStreamReader(f.openInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = r.readLine()) != null) {
                    e.getValue().add(line);
                }
                r.close();
            } catch (FileNotFoundException | NoSuchFileException x) {
                // doesn't exist
            } catch (IOException x) {
                processingEnv
                        .getMessager()
                        .printMessage(Kind.ERROR, "Failed to load existing service definition files: " + x);
            }
        }

        // now write them back out
        for (Map.Entry<String, Set<String>> e : services.entrySet()) {
            try {
                String contract = e.getKey();
                processingEnv.getMessager().printMessage(Kind.NOTE, "Writing META-INF/services/" + contract);
                FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + contract);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), StandardCharsets.UTF_8));
                for (String value : e.getValue()) {
                    pw.println(value);
                }
                pw.close();
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write service definition files: " + x);
            }
        }

        return false;
    }

    private Collection<TypeElement> getContracts(TypeElement type, AutoService a) {
        List<TypeElement> typeElementList = new ArrayList<TypeElement>();

        // explicitly specified?
        try {
            a.value();
            throw new AssertionError();
        } catch (MirroredTypesException e) {
            for (TypeMirror m : e.getTypeMirrors()) {
                if (m.getKind() == TypeKind.VOID) {
                    // contract inferred from the signature
                    boolean hasBaseClass =
                            type.getSuperclass().getKind() != TypeKind.NONE && !isObject(type.getSuperclass());
                    boolean hasInterfaces = !type.getInterfaces().isEmpty();
                    if (hasBaseClass ^ hasInterfaces) {
                        if (hasBaseClass) {
                            typeElementList.add((TypeElement) ((DeclaredType) type.getSuperclass()).asElement());
                        } else {
                            typeElementList.add((TypeElement)
                                    ((DeclaredType) type.getInterfaces().get(0)).asElement());
                        }
                        continue;
                    }
                    error(type, "Contract type was not specified, but it couldn't be inferred.");
                    continue;
                }

                if (m instanceof DeclaredType) {
                    DeclaredType dt = (DeclaredType) m;
                    typeElementList.add((TypeElement) dt.asElement());
                } else {
                    error(type, "Invalid type specified as the contract");
                }
            }
        }
        return typeElementList;
    }

    private boolean isObject(TypeMirror t) {
        if (t instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) t;
            return ((TypeElement) dt.asElement()).getQualifiedName().toString().equals("java.lang.Object");
        }
        return false;
    }

    private void error(Element source, String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR, msg, source);
    }
}