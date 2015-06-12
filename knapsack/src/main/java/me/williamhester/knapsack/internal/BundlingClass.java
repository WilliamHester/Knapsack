package me.williamhester.knapsack.internal;

import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import javax.lang.model.element.Element;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by william on 6/11/15.
 */
class BundlingClass {


    private static final String BINDER_FQCN = "android.os.Binder";
    private static final String BUNDLE_FQCN = "android.os.Bundle";
    private static final String PARCELABLE_FQCN = "android.os.Parcelable";
    private static final String PERSISTABLE_BUNDLE_FQCN = "android.os.PersistableBundle";
    private static final String SIZE_FQCN = "android.util.Size";
    private static final String SIZE_F_FQCN = "android.util.SizeF";
    private static final String ARRAY_LIST_FQCN = "java.util.ArrayList";
    private static final String SPARSE_ARRAY_FQCN = "android.util.SparseArray";
    private static final String SERIALIZABLE_FQCN = "java.io.Serializable";

    private final List<Element> fields = new ArrayList<>();

    private final String classPackage;
    private final String className;
    private final String targetClass;
    private String parentBundler;

    BundlingClass(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
    }

    void addField(Element element) {
        fields.add(element);
    }

    void setParentBundler(String parentBundler) {
        this.parentBundler = parentBundler;
    }

    String writeJava() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code for Knapsack. DO NOT MODIFY!\n")
                .append("package ").append(classPackage).append(";\n\n");
        
        emitImports(builder);

        builder.append("\n");
        builder.append("public class ").append(className);

        if (parentBundler != null) {
            builder.append(" extends ").append(parentBundler);
        } else {
            builder.append(" implements Bundler");
        }
        builder.append(" {\n");

        emitSaveMethod(builder);
        builder.append("\n");
        emitRestoreMethod(builder);

        builder.append("}\n");

        return builder.toString();
    }
    
    private void emitImports(StringBuilder builder) {
        Set<String> imports = new LinkedHashSet<>();
        
        for (Element element : fields) {
            String className = element.asType().toString();
            try {
                Class clazz = Class.forName(className);
                getBundleTypeForClass(clazz, imports);
            } catch (ClassNotFoundException e) {
                
            }
        }

        for (String imp : imports) {
            builder.append("import ").append(imp).append(";\n");
        }
    }

    private void emitSaveMethod(StringBuilder builder) {
        builder.append("    @Override\n")
                .append("    public void save(Bundle state) {\n");

        if (parentBundler != null) {
            builder.append("        super.save(state);\n\n");
        }

    }

    private void emitRestoreMethod(StringBuilder stringBuilder) {

    }

    public void getBundleTypeForClass(Class clazz, Set<String> imports) {
        if (PersistableBundle.class.isInstance(clazz)) {
            imports.add(PERSISTABLE_BUNDLE_FQCN);
        } else if (Binder.class.isInstance(clazz)) {
            imports.add(BINDER_FQCN);
        } else if (Bundle.class.isInstance(clazz)) {
            imports.add(BUNDLE_FQCN);
        } else if (ArrayList.class.isInstance(clazz)) {
            imports.add(ARRAY_LIST_FQCN);
            if (Parcelable.class.isInstance(clazz.getGenericType())) {
                imports.add(PARCELABLE_FQCN);
            }
        } else if (Parcelable.class.isInstance(clazz)) {
            imports.add(PARCELABLE_FQCN);
        } else if (Serializable.class.isInstance(clazz)) {
            imports.add(SERIALIZABLE_FQCN);
        } else if (Size.class.isInstance(clazz)) {
            imports.add(SIZE_FQCN);
        } else if (SizeF.class.isInstance(clazz)) {
            imports.add(SIZE_F_FQCN);
        } else if (SparseArray.class.isInstance(clazz)) {
            imports.add(SPARSE_ARRAY_FQCN);
        }
    }
}
