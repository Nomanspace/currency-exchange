package org.nomanspace.currencyexchange.repository;


import java.util.List;
import java.util.Optional;

public interface Repository <T, K> {

    List<T> findAll() ; //нужен ли тут оптионал?
    Optional<T> findById(K id) ;
    Optional<T> save(T entity) ;
    boolean delete(K id) ;
    Optional<T> update(T entity);
}
