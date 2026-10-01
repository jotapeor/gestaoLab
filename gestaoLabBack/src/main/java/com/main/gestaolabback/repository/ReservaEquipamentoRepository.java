package com.main.gestaolabback.repository;

import com.main.gestaolabback.model.ReservaEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaEquipamentoRepository extends JpaRepository<ReservaEquipamento, Long> {
}
