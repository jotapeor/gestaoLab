package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.LaboratorioRequest;
import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.repository.LaboratorioRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaboratorioServiceTest {

    @Mock
    LaboratorioRepository repository;

    @InjectMocks
    LaboratorioService service;

    @Test
    void criar_nomeDuplicado_lanca409() {
        when(repository.existsByNomeIgnoreCase("Lab A")).thenReturn(true);
        assertThatThrownBy(() -> service.criar(new LaboratorioRequest("Lab A", null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void criar_nomeTrimado_verificaNomeTrimado() {
        when(repository.existsByNomeIgnoreCase("Lab A")).thenReturn(false);
        Laboratorio salvo = new Laboratorio();
        salvo.setNome("Lab A");
        when(repository.save(any())).thenReturn(salvo);
        service.criar(new LaboratorioRequest("  Lab A  ", null, null, null));
        verify(repository).existsByNomeIgnoreCase("Lab A");
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
        assertThatThrownBy(() -> service.atualizar(99L, new LaboratorioRequest("Lab B", null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void atualizar_nomeDuplicadoDeOutro_lanca409() {
        Laboratorio existente = new Laboratorio();
        existente.setNome("Lab X");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByNomeIgnoreCaseAndIdNot("Lab A", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.atualizar(1L, new LaboratorioRequest("Lab A", null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void inativar_setsAtivoFalse() {
        Laboratorio lab = new Laboratorio();
        lab.setAtivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(lab));
        when(repository.save(lab)).thenReturn(lab);
        service.inativar(1L);
        assertThat(lab.isAtivo()).isFalse();
    }

    @Test
    void reativar_setsAtivoTrue() {
        Laboratorio lab = new Laboratorio();
        lab.setAtivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(lab));
        when(repository.save(lab)).thenReturn(lab);
        service.reativar(1L);
        assertThat(lab.isAtivo()).isTrue();
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
