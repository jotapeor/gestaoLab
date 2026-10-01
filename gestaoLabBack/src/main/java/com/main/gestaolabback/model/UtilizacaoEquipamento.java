package com.main.gestaolabback.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilizacao_equipamento")
public class UtilizacaoEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilizacao_equipamento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipamento", nullable = false)
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva_equipamento")
    private ReservaEquipamento reservaEquipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilizacao")
    private UtilizacaoLaboratorio utilizacaoLaboratorio;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "duracao_minutos", insertable = false, updatable = false)
    private Integer duracaoMinutos;

    private String observacoes;

    public UtilizacaoEquipamento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ReservaEquipamento getReservaEquipamento() {
        return reservaEquipamento;
    }

    public void setReservaEquipamento(ReservaEquipamento reservaEquipamento) {
        this.reservaEquipamento = reservaEquipamento;
    }

    public UtilizacaoLaboratorio getUtilizacaoLaboratorio() {
        return utilizacaoLaboratorio;
    }

    public void setUtilizacaoLaboratorio(UtilizacaoLaboratorio utilizacaoLaboratorio) {
        this.utilizacaoLaboratorio = utilizacaoLaboratorio;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
