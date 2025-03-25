package edu.cit.serbisyo.controller;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserAuthController {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userAuthId;

}
