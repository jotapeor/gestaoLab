package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.LaboratorioRequest;
import com.main.gestaolabback.dto.LaboratorioResponse;
import com.main.gestaolabback.model.Laboratorio;
import com.main.gestaolabback.repository.LaboratorioRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LaboratorioService {

    private final LaboratorioRepository laboratorioRepository;

    public LaboratorioService(LaboratorioRepository laboratorioRepository) {
        this.laboratorioRepository = laboratorioRepository;
    }

    public LaboratorioResponse criar(LaboratorioRequest request) {
        String nome = request.nome().trim();
        if (laboratorioRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um laboratório com o nome \"" + nome + "\".");
        }
        Laboratorio entidade = new Laboratorio();
        entidade.setNome(nome);
        entidade.setLocalizacao(request.localizacao() != null ? request.localizacao().trim() : null);
        entidade.setCapacidade(request.capacidade());
        entidade.setAtivo(request.ativo() != null ? request.ativo() : true);
        return toResponse(laboratorioRepository.save(entidade));
    }

    public List<LaboratorioResponse> listar(String nome, Boolean ativo) {
        String nomeFiltro = (nome != null && !nome.isBlank()) ? nome.trim() : null;
        return laboratorioRepository.findWithFilters(nomeFiltro, ativo)
                .stream().map(this::toResponse).toList();
    }

    public LaboratorioResponse buscarPorId(Long id) {
        return toResponse(laboratorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Laboratório não encontrado.")));
    }

    public LaboratorioResponse atualizar(Long id, LaboratorioRequest request) {
        Laboratorio entidade = laboratorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Laboratório não encontrado."));
        String nome = request.nome().trim();
        if (laboratorioRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um laboratório com o nome \"" + nome + "\".");
        }
        entidade.setNome(nome);
        entidade.setLocalizacao(request.localizacao() != null ? request.localizacao().trim() : null);
        entidade.setCapacidade(request.capacidade());
        return toResponse(laboratorioRepository.save(entidade));
    }

    public void inativar(Long id) {
        Laboratorio entidade = laboratorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Laboratório não encontrado."));
        entidade.setAtivo(false);
        laboratorioRepository.save(entidade);
    }

    public void reativar(Long id) {
        Laboratorio entidade = laboratorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Laboratório não encontrado."));
        entidade.setAtivo(true);
        laboratorioRepository.save(entidade);
    }

    private LaboratorioResponse toResponse(Laboratorio l) {
        return new LaboratorioResponse(l.getId(), l.getNome(), l.getLocalizacao(),
                l.getCapacidade(), l.isAtivo(), l.getDataCadastro());
    }
}
