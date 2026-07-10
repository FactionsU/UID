package dev.kitteh.factions.annotation.processor;

import dev.kitteh.factions.config.annotation.WipeOnReload;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("dev.kitteh.factions.config.annotation.WipeOnReload")
public class WipeOnReloadProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element field : roundEnv.getElementsAnnotatedWith(WipeOnReload.class)) {
            if (!field.getModifiers().contains(Modifier.TRANSIENT)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@WipeOnReload may only be applied to transient fields, but '" + field.getSimpleName() + "' is not transient.", field);
            }
        }
        return true;
    }
}
