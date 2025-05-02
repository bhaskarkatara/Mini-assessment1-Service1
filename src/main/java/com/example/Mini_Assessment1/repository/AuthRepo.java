package com.example.Mini_Assessment1.repository;

import com.example.Mini_Assessment1.dataClass.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepo extends MongoRepository<User,String> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

 }