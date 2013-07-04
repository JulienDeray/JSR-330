/**
 * @author julien
 */

package com.serli.jderay.jsr330.exceptions;


public class FinalFieldException extends Exception {

    public FinalFieldException() {
        super( "A field annotated \"@Inject\" is final. Cannot be injected." );
    }
}
