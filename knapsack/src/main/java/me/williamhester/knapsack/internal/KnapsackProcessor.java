package me.williamhester.knapsack.internal;

import me.williamhester.knapsack.Save;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by william on 6/11/15.
 */
public final class KnapsackProcessor extends AbstractProcessor {

    static final String BINDER_FQCN = "android.os.Binder";
    static final String BUNDLE_FQCN = "android.os.Bundle";
    static final String PARCELABLE_FQCN = "android.os.Parcelable";
    static final String SIZE_FQCN = "android.util.Size";
    static final String SIZE_F_FQCN = "android.util.SizeF";
    static final String ARRAY_LIST_FQCN = "java.util.ArrayList";
    static final String SPARSE_ARRAY_FQCN = "android.util.SparseArray";
    static final String SERIALIZABLE_FQCN = "java.io.Serializable";
    static final String INTEGER_FQCN = "java.lang.Integer";
    static final String BOOLEAN_FQCN = "java.lang.Boolean";
    static final String DOUBLE_FQCN = "java.lang.Double";
    static final String LONG_FQCN = "java.lang.Long";
    static final String BYTE_FQCN = "java.lang.Byte";
    static final String FLOAT_FQCN = "java.lang.Float";
    static final String SHORT_FQCN = "java.lang.Short";
    static final String CHARACTER_FQCN = "java.lang.Character";
    static final String STRING_FQCN = "java.lang.String";
    static final String CHAR_SEQUENCE_FQCN = "java.lang.CharSequence";

    private TypeMirror arrayList;
    private TypeMirror bundle;
    private TypeMirror binder;
    private TypeMirror parcelable;
    private TypeMirror size;
    private TypeMirror sizeF;
    private TypeMirror sparseArray;
    private TypeMirror serializable;
    private TypeMirror intType;
    private TypeMirror booleanType;
    private TypeMirror doubleType;
    private TypeMirror longType;
    private TypeMirror byteType;
    private TypeMirror floatType;
    private TypeMirror shortType;
    private TypeMirror charType;
    private TypeMirror string;
    private TypeMirror charSequence;

    public static final String ANDROID_PREFIX = "android.";
    public static final String JAVA_PREFIX = "java.";
    public static final String SUFFIX = "$$Bundler";

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();

        bundle = elementUtils.getTypeElement(BUNDLE_FQCN).asType();
        binder = elementUtils.getTypeElement(BINDER_FQCN).asType();
        parcelable = elementUtils.getTypeElement(PARCELABLE_FQCN).asType();
        size = elementUtils.getTypeElement(SIZE_FQCN).asType();
        sizeF = elementUtils.getTypeElement(SIZE_F_FQCN).asType();
        arrayList = elementUtils.getTypeElement(ARRAY_LIST_FQCN).asType();
        sparseArray = elementUtils.getTypeElement(SPARSE_ARRAY_FQCN).asType();
        serializable = elementUtils.getTypeElement(SERIALIZABLE_FQCN).asType();
        intType = elementUtils.getTypeElement(INTEGER_FQCN).asType();
        booleanType = elementUtils.getTypeElement(BOOLEAN_FQCN).asType();
        doubleType = elementUtils.getTypeElement(DOUBLE_FQCN).asType();
        longType = elementUtils.getTypeElement(LONG_FQCN).asType();
        byteType = elementUtils.getTypeElement(BYTE_FQCN).asType();
        floatType = elementUtils.getTypeElement(FLOAT_FQCN).asType();
        shortType = elementUtils.getTypeElement(SHORT_FQCN).asType();
        charType = elementUtils.getTypeElement(CHARACTER_FQCN).asType();
        string = elementUtils.getTypeElement(STRING_FQCN).asType();
        charSequence = elementUtils.getTypeElement(CHAR_SEQUENCE_FQCN).asType();
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

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
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

        FieldBundling bundling = getCompatibleType(element, elementType);
        if (bundling == null) {
            error(element, "@Save can only be used on types that are compatible with Bundle's normal types.");
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

        bundlingClass.addField(bundling);

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

    private FieldBundling getCompatibleType(Element element, TypeMirror elementType) {
        FieldBundling bundling = null;
        String variableName = element.getSimpleName().toString();
        if (elementType instanceof ArrayType) {
            bundling = getCompatibleArrayType(((ArrayType) elementType).getComponentType(), variableName);
        } else if (typeUtils.isAssignable(elementType, arrayList)) {
            List<? extends TypeMirror> args = ((DeclaredType) elementType).getTypeArguments();
            if (args.size() != 1) {
                return null;
            }
            bundling = getCompatibleArrayListType(args.get(0), variableName);
        } else if (typeUtils.isAssignable(elementType, sparseArray)) {
            List<? extends TypeMirror> args = ((DeclaredType) elementType).getTypeArguments();
            if (args.size() == 1 && typeUtils.isAssignable(args.get(0), parcelable)) {
                bundling = new SimpleFieldBundling("SparseParcelableArray", variableName);
            }
        } else if (typeUtils.isAssignable(elementType, serializable)) {
            bundling = new SerializableFieldBundling(elementType.toString(), variableName);
        } else if (typeUtils.isAssignable(elementType, bundle)) {
            bundling = new SimpleFieldBundling("Bundle", variableName);
        } else if (typeUtils.isAssignable(elementType, binder)) {
            bundling = new SimpleFieldBundling("Binder", variableName);
        } else if (typeUtils.isAssignable(elementType, parcelable)) {
            bundling = new SimpleFieldBundling("Parcelable", variableName);
        } else if (typeUtils.isAssignable(elementType, size)) {
            bundling = new SimpleFieldBundling("Size", variableName);
        } else if (typeUtils.isAssignable(elementType, sizeF)) {
            bundling = new SimpleFieldBundling("SizeF", variableName);
        } else if (typeUtils.isAssignable(elementType, intType)) {
            bundling = new SimpleFieldBundling("Int", variableName);
        } else if (typeUtils.isAssignable(elementType, booleanType)) {
            bundling = new SimpleFieldBundling("Boolean", variableName);
        } else if (typeUtils.isAssignable(elementType, doubleType)) {
            bundling = new SimpleFieldBundling("Double", variableName);
        } else if (typeUtils.isAssignable(elementType, longType)) {
            bundling = new SimpleFieldBundling("Long", variableName);
        } else if (typeUtils.isAssignable(elementType, byteType)) {
            bundling = new SimpleFieldBundling("Byte", variableName);
        } else if (typeUtils.isAssignable(elementType, floatType)) {
            bundling = new SimpleFieldBundling("Float", variableName);
        } else if (typeUtils.isAssignable(elementType, shortType)) {
            bundling = new SimpleFieldBundling("Short", variableName);
        } else if (typeUtils.isAssignable(elementType, charType)) {
            bundling = new SimpleFieldBundling("Char", variableName);
        } else if (typeUtils.isAssignable(elementType, string)) {
            bundling = new SimpleFieldBundling("String", variableName);
        } else if (typeUtils.isAssignable(elementType, charSequence)) {
            bundling = new SimpleFieldBundling("CharSequence", variableName);
        }
        return bundling;
    }

    private FieldBundling getCompatibleArrayType(TypeMirror componentType, String variableName) {
        FieldBundling bundling = null;
        if (typeUtils.isAssignable(componentType, byteType)) {
            bundling = new ArrayFieldBundling("Byte", variableName);
        } else if (typeUtils.isAssignable(componentType, shortType)) {
            bundling = new ArrayFieldBundling("Short", variableName);
        } else if (typeUtils.isAssignable(componentType, intType)) {
            bundling = new ArrayFieldBundling("Int", variableName);
        } else if (typeUtils.isAssignable(componentType, longType)) {
            bundling = new ArrayFieldBundling("Long", variableName);
        } else if (typeUtils.isAssignable(componentType, charType)) {
            bundling = new ArrayFieldBundling("Char", variableName);
        } else if (typeUtils.isAssignable(componentType, booleanType)) {
            bundling = new ArrayFieldBundling("Boolean", variableName);
        } else if (typeUtils.isAssignable(componentType, floatType)) {
            bundling = new ArrayFieldBundling("Float", variableName);
        } else if (typeUtils.isAssignable(componentType, doubleType)) {
            bundling = new ArrayFieldBundling("Double", variableName);
        } else if (typeUtils.isAssignable(componentType, string)) {
            bundling = new ArrayFieldBundling("String", variableName);
        } else if (typeUtils.isAssignable(componentType, charSequence)) {
            bundling = new ArrayFieldBundling("CharSequence", variableName);
        } else if (typeUtils.isAssignable(componentType, parcelable)) {
            bundling = new ArrayFieldBundling("Parcelable", variableName);
        }
        return bundling;
    }

    private FieldBundling getCompatibleArrayListType(TypeMirror componentType, String variableName) {
        FieldBundling bundling = null;
        if (typeUtils.isAssignable(componentType, string)) {
            bundling = new ArrayListFieldBundling("String", variableName);
        } else if (typeUtils.isAssignable(componentType, charSequence)) {
            bundling = new ArrayFieldBundling("CharSequence", variableName);
        } else if (typeUtils.isAssignable(componentType, intType)) {
            bundling = new ArrayFieldBundling("Integer", variableName);
        } else if (typeUtils.isAssignable(componentType, parcelable)) {
            bundling = new ArrayFieldBundling("Parcelable", variableName);
        } else if (typeUtils.isAssignable(componentType, serializable)) {
            bundling = new SerializableFieldBundling("ArrayList<" + componentType.toString() + ">", variableName);
        }
        return bundling;
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
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

}
