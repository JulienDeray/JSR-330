/**
 * @author julien
 */

package com.serli.jderay.jsr330.exceptions;


public class DoesNotImplementException extends Exception {

    public DoesNotImplementException(String clazzToImpl, String implementation) {
        super(implementation + " does not implements " + clazzToImpl);
    }
}
