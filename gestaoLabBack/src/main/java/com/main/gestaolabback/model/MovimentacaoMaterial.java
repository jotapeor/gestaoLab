package com.main.gestaolabback.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_material")
public class MovimentacaoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registrado_por")
    private Usuario registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilizacao")
    private UtilizacaoLaboratorio utilizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimentacao_origem")
    private MovimentacaoMaterial movimentacaoOrigem;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipo;

    private BigDecimal quantidade;

    private String observacao;

    @Column(name = "data_movimentacao")
    private LocalDateTime dataMovimentacao;

    public MovimentacaoMaterial() {
    }

    @PrePersist
    private void prePersist() {
        if (dataMovimentacao == null) dataMovimentacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public UtilizacaoLaboratorio getUtilizacao() {
        return utilizacao;
    }

    public void setUtilizacao(UtilizacaoLaboratorio utilizacao) {
        this.utilizacao = utilizacao;
    }

    public MovimentacaoMaterial getMovimentacaoOrigem() {
        return movimentacaoOrigem;
    }

    public void setMovimentacaoOrigem(MovimentacaoMaterial movimentacaoOrigem) {
        this.movimentacaoOrigem = movimentacaoOrigem;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }
}
