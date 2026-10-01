package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.UtilizacaoEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilizacaoEquipamentoRepository extends JpaRepository<UtilizacaoEquipamento, Long> {
}
