package com.benchmark.registry;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface MovieRepository extends CrudRepository<Movie, String> {
//    @Query("SELECT * FROM movie_information WHERE TITLE LIKE )
    List<Movie> findByTitleLike(String title);
}