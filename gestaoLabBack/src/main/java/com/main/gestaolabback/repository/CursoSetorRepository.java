package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.CursoSetor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoSetorRepository extends JpaRepository<CursoSetor, Long> {
}
