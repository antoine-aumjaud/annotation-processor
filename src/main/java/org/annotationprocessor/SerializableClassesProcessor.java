package org.annotationprocessor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("ged.salestools.common.control.SerializableClasses")
public class SerializableClassesProcessor extends AbstractAnnotationProcessor {
  private Elements elementUtils;
  private Types typeUtils;
  private Messager messager;

  private TypeMirror serializableType;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    messager = processingEnv.getMessager();

    // The Serializable interface - used for comparison
    serializableType = elementUtils.getTypeElement(Serializable.class.getCanonicalName()).asType();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      // We're not interested in the postprocessing round.
      return false;
    }

    Set<? extends Element> rootElements = roundEnv
        .getElementsAnnotatedWith(SerializableClasses.class);
    for (Element element : rootElements) {

      // We're only interested in packages
      if (element.getKind() != ElementKind.PACKAGE) {
        continue;
      }

      // Get some info on the annotated package
      PackageElement thePackage = elementUtils.getPackageOf(element);
      String packageName = thePackage.getQualifiedName().toString();

      // Test each class in the package for "serializability"
      List<? extends Element> classes = thePackage.getEnclosedElements();
      for (Element theClass : classes) {
        // We're not interested in interfaces
        if (theClass.getKind() == ElementKind.INTERFACE) {
          continue;
        }

        // Check if the class is actually Serializable
        boolean isSerializable = typeUtils.isAssignable(theClass.asType(), serializableType);
        if (!isSerializable) {
          messager.printMessage(Kind.ERROR,
              "SerializableClasses: The following class is not Serializable: " + packageName + "."
                  + theClass.getSimpleName());
        }
      }
    }

    // Prevent other processors from processing this annotation
    return true;
  }
}