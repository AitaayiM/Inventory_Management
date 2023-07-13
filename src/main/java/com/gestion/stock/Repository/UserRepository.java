package com.gestion.stock.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gestion.stock.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{

    User findByUsername(String username);
    
}
