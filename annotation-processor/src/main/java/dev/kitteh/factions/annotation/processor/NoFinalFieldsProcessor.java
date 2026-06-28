package dev.kitteh.factions.annotation.processor;

import dev.kitteh.factions.annotation.NoFinalFields;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("dev.kitteh.factions.annotation.NoFinalFields")
public class NoFinalFieldsProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotated : roundEnv.getElementsAnnotatedWith(NoFinalFields.class)) {
            if (annotated instanceof TypeElement type) {
                TypeElement current = type;
                while (current != null && !current.getQualifiedName().contentEquals("java.lang.Object")) {
                    for (Element enclosed : current.getEnclosedElements()) {
                        if (enclosed.getKind() != ElementKind.FIELD || !(enclosed instanceof VariableElement field)) {
                            continue;
                        }
                        Set<Modifier> modifiers = field.getModifiers();
                        if (!modifiers.contains(Modifier.FINAL)) {
                            continue;
                        }
                        if (modifiers.contains(Modifier.STATIC)) {
                            continue;
                        }
                        String inherited = current.equals(type) ? "" : " (inherited from %s)".formatted(current.getQualifiedName());
                        String message = "@NoFinalFields violated: field '%s' in %s is final and my heart is broken"
                                .formatted(field.getSimpleName(), type.getQualifiedName() + inherited);

                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, current.equals(type) ? field : type);
                    }
                    current = getSuperIfExists(current);
                }
            }
        }
        return true;
    }

    private TypeElement getSuperIfExists(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE || !(superclass instanceof DeclaredType declared)) {
            return null;
        }
        return declared.asElement() instanceof TypeElement element ? element : null;
    }
}
