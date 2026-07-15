package com.github.playernguyen.processor;

import com.github.playernguyen.inject.Component;
import com.github.playernguyen.inject.Inject;
import com.github.playernguyen.inject.Singleton;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Annotation processor that generates injection providers for components marked with @Component.
 */
@AutoService(javax.annotation.processing.Processor.class)
@SupportedAnnotationTypes({
    "com.github.playernguyen.inject.Component",
    "com.github.playernguyen.inject.Inject",
    "com.github.playernguyen.inject.Singleton"
})
public class InjectionProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                try {
                    generateProvider(typeElement);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to generate injection provider: " + e.getMessage(),
                        element
                    );
                }
            }
        }
        return true;
    }

    /**
     * Sub-package appended to the component's package when emitting generated provider classes.
     * Keeping generated sources in their own sub-package avoids name collisions with any
     * hand-written class whose name happens to end in "Provider".
     */
    static final String GENERATED_SUBPACKAGE = "generated";

    private void generateProvider(TypeElement typeElement) throws IOException {
        String componentPackage = getPackageName(typeElement);
        String packageName = componentPackage + "." + GENERATED_SUBPACKAGE;
        String className = typeElement.getSimpleName().toString();
        String providerClassName = className + "Provider";

        StringBuilder code = new StringBuilder();
        code.append("package ").append(packageName).append(";\n\n");
        // Import the component class from its original package so the generated
        // provider can reference it by simple name.
        code.append("import ").append(componentPackage).append(".").append(className).append(";\n");
        code.append("import com.github.playernguyen.inject.InjectionPoint;\n");
        code.append("import com.github.playernguyen.runtime.InjectionContainer;\n\n");
        code.append("public class ").append(providerClassName).append(" implements InjectionPoint {\n");
        code.append("    @Override\n");
        code.append("    public Object provide(Object container) {\n");
        code.append("        InjectionContainer injectionContainer = (InjectionContainer) container;\n");

        // Find constructor with @Inject or default constructor
        ExecutableElement constructor = findInjectConstructor(typeElement);

        if (constructor != null && constructor.getParameters().size() > 0) {
            // Constructor injection
            code.append("        ").append(className).append(" instance = new ").append(className).append("(");
            boolean first = true;
            for (VariableElement param : constructor.getParameters()) {
                if (!first) code.append(", ");
                String paramType = param.asType().toString();
                String qualifierName = "";
                Inject inject = param.getAnnotation(Inject.class);
                if (inject != null && !inject.value().isEmpty()) {
                    qualifierName = inject.value();
                }
                code.append("(").append(paramType).append(") ")
                    .append("injectionContainer.get(\"").append(qualifierName.isEmpty() ? paramType : qualifierName).append("\")");
                first = false;
            }
            code.append(");\n");
        } else {
            // No-arg constructor
            code.append("        ").append(className).append(" instance = new ").append(className).append("();\n");
        }

        // Field injection
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosedElement;
                Inject inject = field.getAnnotation(Inject.class);
                if (inject != null) {
                    String fieldType = field.asType().toString();
                    String fieldName = field.getSimpleName().toString();
                    String qualifierName = inject.value().isEmpty() ? fieldType : inject.value();
                    code.append("        instance.").append(fieldName).append(" = (").append(fieldType).append(") ")
                        .append("injectionContainer.get(\"").append(qualifierName).append("\");\n");
                }
            }
        }

        code.append("        return instance;\n");
        code.append("    }\n");
        code.append("}\n");

        // Write the generated source file
        try (Writer writer = processingEnv.getFiler()
            .createSourceFile(packageName + "." + providerClassName, typeElement)
            .openWriter()) {
            writer.write(code.toString());
        }
    }

    private ExecutableElement findInjectConstructor(TypeElement typeElement) {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) element;
                if (constructor.getAnnotation(Inject.class) != null) {
                    return constructor;
                }
            }
        }
        return null;
    }

    private String getPackageName(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString()
            .substring(0, typeElement.getQualifiedName().toString().lastIndexOf('.'));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
