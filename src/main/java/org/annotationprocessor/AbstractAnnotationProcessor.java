package org.annotationprocessor;

import javax.annotation.processing.AbstractProcessor;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor {
  /**
   * Method to add for debugging
   * 1/ add break point on code: "while (me)"
   * 2/ set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=7111,server=y,suspend=n
   * 3/ create java remote application on port 7111 
   */
  protected void breakpoint() {
    boolean me = true;
    try {
      while (me) {
        Thread.sleep(2000);
      }
    } catch (InterruptedException e) {
    }
  }
}
