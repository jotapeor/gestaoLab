package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.CursoSetorRequest;
import com.main.gestaolabback.model.CursoSetor;
import com.main.gestaolabback.model.TipoCursoSetor;
import com.main.gestaolabback.repository.CursoSetorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoSetorServiceTest {

    @Mock
    CursoSetorRepository repository;

    @InjectMocks
    CursoSetorService service;

    @Test
    void criar_nomeDuplicado_lanca409() {
        when(repository.existsByNomeIgnoreCase("Biologia")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void criar_nomeTrimado_verificaNomeTrimado() {
        when(repository.existsByNomeIgnoreCase("Biologia")).thenReturn(false);
        CursoSetor salvo = new CursoSetor();
        salvo.setNome("Biologia");
        salvo.setTipo(TipoCursoSetor.CURSO);
        when(repository.save(any())).thenReturn(salvo);
        service.criar(new CursoSetorRequest("  Biologia  ", TipoCursoSetor.CURSO, null));
        verify(repository).existsByNomeIgnoreCase("Biologia");
    }

    @Test
    void buscarPorId_inexistente_lanca404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void atualizar_inexistente_lanca404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar(99L, new CursoSetorRequest("Bio", TipoCursoSetor.CURSO, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void atualizar_nomeDuplicadoDeOutro_lanca409() {
        CursoSetor existente = new CursoSetor();
        existente.setNome("Fisio");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Biologia", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.atualizar(1L, new CursoSetorRequest("Biologia", TipoCursoSetor.CURSO, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void inativar_setsAtivoFalse() {
        CursoSetor cs = new CursoSetor();
        cs.setAtivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(cs));
        when(repository.save(cs)).thenReturn(cs);
        service.inativar(1L);
        assertThat(cs.isAtivo()).isFalse();
    }

    @Test
    void reativar_setsAtivoTrue() {
        CursoSetor cs = new CursoSetor();
        cs.setAtivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(cs));
        when(repository.save(cs)).thenReturn(cs);
        service.reativar(1L);
        assertThat(cs.isAtivo()).isTrue();
    }

    @Test
    void inativar_inexistente_lanca404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.inativar(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void reativar_inexistente_lanca404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.reativar(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }
}
