package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;

public interface AuthorRepository extends JpaRepository<Author, Long> {}
