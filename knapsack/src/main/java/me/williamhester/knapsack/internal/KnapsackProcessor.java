package me.williamhester.knapsack.internal;

import me.williamhester.knapsack.Save;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by william on 6/11/15.
 */
public class KnapsackProcessor extends AbstractProcessor {

    private static final String ANDROID_PREFIX = "android.";
    private static final String JAVA_PREFIX = "java.";
    private static final String SUFFIX = "$$Bundler";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Save.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BundlingClass> targetClassMap = findAndParseTargets(roundEnv);

        for (Map.Entry<TypeElement, BundlingClass> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BundlingClass bindingClass = entry.getValue();

            try {
                JavaFileObject jfo = filer.createSourceFile(bindingClass.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(bindingClass.writeJava());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());
            }
        }

        return false;
    }

    private Map<TypeElement, BundlingClass> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, BundlingClass> targetClassMap = new LinkedHashMap<>();
        Set<String> erasedTargetNames = new LinkedHashSet<>();

        for (Element element : env.getElementsAnnotatedWith(Save.class)) {
            try {
                parseSave(element, targetClassMap, erasedTargetNames);
            } catch (Exception e) {
                error(element, "There was a problem parsing @Save. Here's the StackTrace:");
                e.printStackTrace();
            }
        }

        return targetClassMap;
    }

    private void parseSave(Element element, Map<TypeElement, BundlingClass> targetClassMap, Set<String> erasedTargetNames) {
        boolean hasError = false;

        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        TypeMirror elementType = element.asType();
        if (elementType.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) elementType;
            elementType = typeVariable.getUpperBound();
        }

        if (!isCompatibleType(elementType)) {
            error(element, "@Save can only be used on types that are compatible with Bundle's normal types.",
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        hasError |= isInaccessibleViaGeneratedCode(Save.class, "fields", element);
        hasError |= isBindingInWrongPackage(Save.class, element);

        if (hasError) {
            return;
        }

        BundlingClass bundlingClass = targetClassMap.get(enclosingElement);
        if (bundlingClass == null) {
            createNewBundlingClass(targetClassMap, enclosingElement);
            bundlingClass = targetClassMap.get(enclosingElement);
        }

        bundlingClass.addField(element);

        erasedTargetNames.add(enclosingElement.toString());
    }

    private BundlingClass createNewBundlingClass(Map<TypeElement, BundlingClass> targetClassMap, TypeElement enclosingElement) {
        String targetType = enclosingElement.getQualifiedName().toString();
        String classPackage = getPackageName(enclosingElement);
        String className = getClassName(enclosingElement, classPackage) + SUFFIX;

        BundlingClass bundlingClass = new BundlingClass(classPackage, className, targetType);
        targetClassMap.put(enclosingElement, bundlingClass);
        return bundlingClass;
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        // Verify method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            error(element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing type.
        if (enclosingElement.getKind() != ElementKind.CLASS) {
            error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        // Verify containing class visibility is not private.
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(),
                    element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }

    private boolean isBindingInWrongPackage(Class<? extends Annotation> annotationClass,
                                            Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith(ANDROID_PREFIX)) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith(JAVA_PREFIX)) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }

    private boolean isCompatibleType(TypeMirror elementType) {
        return Math.random() > 0.5; // Wildcard, bitches
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

}
