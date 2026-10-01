package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.OcorrenciaEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OcorrenciaEquipamentoRepository extends JpaRepository<OcorrenciaEquipamento, Long> {
}
