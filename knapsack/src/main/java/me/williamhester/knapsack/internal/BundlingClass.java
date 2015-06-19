package me.williamhester.knapsack.internal;

import java.util.*;

/**
 * Created by william on 6/11/15.
 */
class BundlingClass {

    private static final String BINDER_FQCN = "android.os.Binder";
    private static final String BUNDLE_FQCN = "android.os.Bundle";
    private static final String PARCELABLE_FQCN = "android.os.Parcelable";
    private static final String SIZE_FQCN = "android.util.Size";
    private static final String SIZE_F_FQCN = "android.util.SizeF";
    private static final String ARRAY_LIST_FQCN = "java.util.ArrayList";
    private static final String SPARSE_ARRAY_FQCN = "android.util.SparseArray";
    private static final String SERIALIZABLE_FQCN = "java.io.Serializable";
    private static final String BUNDLER_FQCN = "me.williamhester.knapsack.Bundler";

    private static final String[] imports = new String[] {
            BINDER_FQCN, BUNDLE_FQCN, PARCELABLE_FQCN, SIZE_FQCN, SIZE_F_FQCN,
            ARRAY_LIST_FQCN, SPARSE_ARRAY_FQCN, SERIALIZABLE_FQCN, BUNDLER_FQCN
    };

    private final List<FieldBundling> fields = new ArrayList<>();

    private final String classPackage;
    private final String className;
    private final String targetClass;
    private String parentBundler;

    BundlingClass(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
    }

    String getFqcn() {
        return classPackage + "." + className;
    }

    void addField(FieldBundling element) {
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
        for (String imp : imports) {
            builder.append("import ").append(imp).append(";\n");
        }
    }

    private void emitSaveMethod(StringBuilder builder) {
        builder.append("    @Override\n")
                .append("    public void save(Bundle state, Object in) {\n");

        if (parentBundler != null) {
            builder.append("        super.save(state, in);\n\n");
        } else {
            builder.append("\n");
        }
        builder.append("        ")
                .append(targetClass).append(" instance = (").append(targetClass).append(") in;\n");
        for (FieldBundling bundling : fields) {
            builder.append("        state.put")
                    .append(bundling.getBundleMethodPhrase()).append("(\"").append(classPackage).append(".")
                            .append(className).append(".").append(bundling.getName()).append("\", instance.")
                            .append(bundling.getName()).append(");\n");
        }
        builder.append("    }\n");
    }

    private void emitRestoreMethod(StringBuilder builder) {
        builder.append("    @Override\n")
                .append("    public void restore(Bundle state, Object in) {\n");

        if (parentBundler != null) {
            builder.append("        super.restore(state, in);\n\n");
        } else {
            builder.append("\n");
        }
        builder.append("        ")
                .append(targetClass).append(" instance = (").append(targetClass).append(") in;\n");
        for (FieldBundling bundling : fields) {
            builder.append("        instance.")
                    .append(bundling.getName())
                    .append(" = ");

            if (bundling.requiresCast()) {
                builder.append("(")
                        .append(bundling.getType())
                        .append(") ");
            }

            builder.append("state.get")
                    .append(bundling.getBundleMethodPhrase())
                    .append("(\"").append(classPackage).append(".").append(className).append(".")
                    .append(bundling.getName()).append("\");\n");
        }

        builder.append("    }\n");
    }


}
