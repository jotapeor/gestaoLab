package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.MovimentacaoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimentacaoMaterialRepository extends JpaRepository<MovimentacaoMaterial, Long> {
}
