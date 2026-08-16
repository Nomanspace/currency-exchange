package org.nomanspace.currencyexchange.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.nomanspace.currencyexchange.exception.MethodNotAllowedException;

public interface Handler {

    default void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        throw new MethodNotAllowedException();
    }

    default void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        throw new MethodNotAllowedException();
    }

    default void doPatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        throw new MethodNotAllowedException();
    }

}
