/**
 * @author julien
 */

package com.serli.jderay.jsr330.exceptions;


public class StaticFieldException extends Exception {

    public StaticFieldException() {
        super( "A field annotated \"@Inject\" is static. Cannot be injected." );
    }
}
