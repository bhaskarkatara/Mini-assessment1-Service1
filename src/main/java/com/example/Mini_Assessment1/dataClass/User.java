package com.example.Mini_Assessment1.dataClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class User {

  @Id
  private ObjectId id;

  private String name;
  private String email;
  private String password;

  public User(String name, String email,String password) {
    this.name = name;
    this.email = email;
    this.password = password;
  }
}
