package com.main.gestaolabback.helper;

import com.main.gestaolabback.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static CursoSetor criarCursoSetor(String nome) {
        CursoSetor cs = new CursoSetor();
        cs.setNome(nome);
        return cs;
    }

    public static Laboratorio criarLaboratorio(String nome) {
        Laboratorio lab = new Laboratorio();
        lab.setNome(nome);
        return lab;
    }

    public static Usuario criarUsuario(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("$2a$10$hashedpassword");
        return u;
    }

    public static Projeto criarProjeto(String titulo, TipoProjeto tipo) {
        Projeto p = new Projeto();
        p.setTitulo(titulo);
        p.setTipo(tipo);
        return p;
    }

    public static Material criarMaterial(Laboratorio lab, String nome, TipoMaterial tipo) {
        Material m = new Material();
        m.setLaboratorio(lab);
        m.setNome(nome);
        m.setTipo(tipo);
        m.setQuantidadeDisponivel(new BigDecimal("10.00"));
        return m;
    }

    public static MovimentacaoMaterial criarMovimentacao(Material material, Usuario usuario,
                                                          TipoMovimentacao tipo, BigDecimal quantidade) {
        MovimentacaoMaterial mov = new MovimentacaoMaterial();
        mov.setMaterial(material);
        mov.setUsuario(usuario);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        return mov;
    }

    public static Equipamento criarEquipamento(Laboratorio lab, String nome) {
        Equipamento eq = new Equipamento();
        eq.setLaboratorio(lab);
        eq.setNome(nome);
        return eq;
    }

    public static OcorrenciaEquipamento criarOcorrencia(Equipamento eq, Usuario usuario,
                                                         TipoOcorrencia tipo) {
        OcorrenciaEquipamento oc = new OcorrenciaEquipamento();
        oc.setEquipamento(eq);
        oc.setUsuario(usuario);
        oc.setTipo(tipo);
        oc.setDescricao("Descrição de teste");
        return oc;
    }

    public static UtilizacaoLaboratorio criarUtilizacaoLaboratorio(Laboratorio lab, Usuario usuario) {
        UtilizacaoLaboratorio ul = new UtilizacaoLaboratorio();
        ul.setLaboratorio(lab);
        ul.setUsuario(usuario);
        ul.setDataEntrada(LocalDateTime.now());
        return ul;
    }
}
