package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.UtilizacaoLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilizacaoLaboratorioRepository extends JpaRepository<UtilizacaoLaboratorio, Long> {
}
