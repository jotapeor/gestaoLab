package com.main.gestaolabback;

import com.main.gestaolabback.helper.TestDataFactory;
import com.main.gestaolabback.model.*;
import com.main.gestaolabback.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EntidadesRepositoriosTest {

    @Autowired private TestEntityManager em;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CursoSetorRepository cursoSetorRepository;
    @Autowired private ProjetoRepository projetoRepository;
    @Autowired private LaboratorioRepository laboratorioRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private MovimentacaoMaterialRepository movimentacaoMaterialRepository;

    @Test
    void salvarUsuario_comCursoSetorResponsavelEProjeto() {
        CursoSetor cs = cursoSetorRepository.save(TestDataFactory.criarCursoSetor("Ciência da Computação"));

        Usuario responsavel = usuarioRepository.save(TestDataFactory.criarUsuario("Prof. Silva", "silva@test.com"));

        Projeto projeto = projetoRepository.save(TestDataFactory.criarProjeto("TCC Gestão", TipoProjeto.TCC_I));

        Usuario usuario = TestDataFactory.criarUsuario("João", "joao@test.com");
        usuario.setCursoSetor(cs);
        usuario.setResponsavel(responsavel);
        usuario.getProjetos().add(projeto);
        usuario = usuarioRepository.save(usuario);

        em.flush();
        em.clear();

        Optional<Usuario> found = usuarioRepository.findById(usuario.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCursoSetor().getId()).isEqualTo(cs.getId());
        assertThat(found.get().getResponsavel().getId()).isEqualTo(responsavel.getId());
        assertThat(found.get().getProjetos()).hasSize(1);
        assertThat(found.get().getProjetos().get(0).getTitulo()).isEqualTo("TCC Gestão");
    }

    @Test
    void salvarMovimentacaoMaterial_comMovimentacaoOrigem() {
        Laboratorio lab = laboratorioRepository.save(TestDataFactory.criarLaboratorio("Lab 101"));
        Material material = materialRepository.save(TestDataFactory.criarMaterial(lab, "Reagente X", TipoMaterial.CONSUMIVEL));
        Usuario usuario = usuarioRepository.save(TestDataFactory.criarUsuario("Maria", "maria@test.com"));

        MovimentacaoMaterial origem = movimentacaoMaterialRepository.save(
                TestDataFactory.criarMovimentacao(material, usuario, TipoMovimentacao.ENTRADA, new BigDecimal("5.00")));

        MovimentacaoMaterial devolucao = TestDataFactory.criarMovimentacao(
                material, usuario, TipoMovimentacao.DEVOLUCAO, new BigDecimal("2.00"));
        devolucao.setMovimentacaoOrigem(origem);
        devolucao = movimentacaoMaterialRepository.save(devolucao);

        em.flush();
        em.clear();

        Optional<MovimentacaoMaterial> found = movimentacaoMaterialRepository.findById(devolucao.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMovimentacaoOrigem().getId()).isEqualTo(origem.getId());
        assertThat(found.get().getTipo()).isEqualTo(TipoMovimentacao.DEVOLUCAO);
    }

    @Test
    void enumsEValoresPadrao_persistidosCorretamente() {
        CursoSetor cs = cursoSetorRepository.save(TestDataFactory.criarCursoSetor("Engenharia"));
        em.flush();
        em.clear();

        CursoSetor foundCs = cursoSetorRepository.findById(cs.getId()).orElseThrow();
        assertThat(foundCs.getTipo()).isEqualTo(TipoCursoSetor.CURSO);
        assertThat(foundCs.isAtivo()).isTrue();
        assertThat(foundCs.getDataCadastro()).isNotNull();

        Usuario usuario = usuarioRepository.save(TestDataFactory.criarUsuario("Ana", "ana@test.com"));
        em.flush();
        em.clear();

        Usuario foundU = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(foundU.getPerfil()).isEqualTo(PerfilUsuario.USUARIO);
        assertThat(foundU.isAtivo()).isTrue();
        assertThat(foundU.isPrimeiroAcesso()).isTrue();
        assertThat(foundU.getDataCriacao()).isNotNull();

        Laboratorio lab = laboratorioRepository.save(TestDataFactory.criarLaboratorio("Lab Química"));
        Material material = materialRepository.save(
                TestDataFactory.criarMaterial(lab, "Béquer", TipoMaterial.REUTILIZAVEL));
        em.flush();
        em.clear();

        Material foundM = materialRepository.findById(material.getId()).orElseThrow();
        assertThat(foundM.getUnidadeMedida()).isEqualTo("un");
        assertThat(foundM.getQuantidadeDisponivel()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(foundM.isAtivo()).isTrue();
        assertThat(foundM.getDataCadastro()).isNotNull();
    }

    @Test
    void findByEmail_encontrandoENaoEncontrando() {
        usuarioRepository.save(TestDataFactory.criarUsuario("Pedro", "pedro@test.com"));
        em.flush();
        em.clear();

        assertThat(usuarioRepository.findByEmail("pedro@test.com")).isPresent();
        assertThat(usuarioRepository.findByEmail("naoexiste@test.com")).isEmpty();
    }
}
