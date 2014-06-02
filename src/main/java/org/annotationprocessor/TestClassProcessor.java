package org.annotationprocessor;

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
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("ged.salestools.common.control.TestClass")
public class TestClassProcessor extends AbstractAnnotationProcessor {
  private Elements elementUtils;
  private Types typeUtils;
  private Messager messager;

  private TypeMirror voidType;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    messager = processingEnv.getMessager();

    // The Serializable interface - used for comparison
    voidType = elementUtils.getTypeElement(Void.class.getCanonicalName()).asType();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      // We're not interested in the postprocessing round.
      return false;
    }

    Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(TestClass.class);
    for (Element element : rootElements) {

      // We're only interested in classes
      if (element.getKind() != ElementKind.CLASS) {
        continue;
      }

      //breakpoint();

      //Get package info
      //- Get package of this element
      PackageElement thePackage = elementUtils.getPackageOf(element);
      //- Get package name
      String packageName = thePackage.getQualifiedName().toString();

      //-----------------------------------------------------------------
      //Get tested class from annotation
      //-----------------------------------------------------------------
      TestClass testClassAnnotation = element.getAnnotation(TestClass.class);

      TypeMirror testClassType = null;
      //http://java.sun.com/javase/6/docs/api/javax/lang/model/element/Element.html#getAnnotation%28java.lang.Class%29
      try {
        testClassAnnotation.value();
      } catch (MirroredTypeException mte) {
        testClassType = mte.getTypeMirror();
      }
      Element testClassElement = typeUtils.asElement(testClassType);

      //Test class has been set (diff of void)
      if (!typeUtils.isAssignable(testClassType, voidType)) {
        //Get package
        PackageElement testClassPackage = elementUtils.getPackageOf(testClassElement);
        String testClassPackageName = testClassPackage.getQualifiedName().toString();
        //Test package
        if (!testClassPackageName.equals(packageName)) {
          messager.printMessage(Kind.WARNING, "TestClass: The class "
              + testClassElement.getSimpleName() + " is in the package " + testClassPackageName
              + " instand of " + packageName);
        }
      }
      //-----------------------------------------------------------------
      //No tested class on annotation found, try to find a class in the same package
      //-----------------------------------------------------------------
      else {
        //Build tested class name
        String simpleName = element.getSimpleName().toString();
        if (element.getSimpleName().toString().endsWith("Test")) {
          simpleName = simpleName.substring(0, simpleName.length() - "Test".length());
        }
        String className = packageName + "." + simpleName;

        //Try to find the test class
        TypeElement buildTestClass = elementUtils.getTypeElement(className);
        if (buildTestClass == null) {
          messager.printMessage(Kind.WARNING, "TestClass: No class " + className + " found");
        }
      }
    }

    // Prevent other processors from processing this annotation
    return true;
  }
}
