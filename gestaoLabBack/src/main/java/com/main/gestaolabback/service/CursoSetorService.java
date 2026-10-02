package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.CursoSetorRequest;
import com.main.gestaolabback.dto.CursoSetorResponse;
import com.main.gestaolabback.model.CursoSetor;
import com.main.gestaolabback.model.TipoCursoSetor;
import com.main.gestaolabback.repository.CursoSetorRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CursoSetorService {

    private final CursoSetorRepository cursoSetorRepository;

    public CursoSetorService(CursoSetorRepository cursoSetorRepository) {
        this.cursoSetorRepository = cursoSetorRepository;
    }

    public CursoSetorResponse criar(CursoSetorRequest request) {
        String nome = request.nome().trim();
        if (cursoSetorRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um curso/setor com o nome \"" + nome + "\".");
        }
        CursoSetor entidade = new CursoSetor();
        entidade.setNome(nome);
        entidade.setTipo(request.tipo() != null ? request.tipo() : TipoCursoSetor.CURSO);
        entidade.setAtivo(request.ativo() != null ? request.ativo() : true);
        return toResponse(cursoSetorRepository.save(entidade));
    }

    public List<CursoSetorResponse> listar(String nome, Boolean ativo, TipoCursoSetor tipo) {
        String nomeFiltro = (nome != null && !nome.isBlank()) ? nome.trim() : null;
        return cursoSetorRepository.findWithFilters(nomeFiltro, ativo, tipo)
                .stream().map(this::toResponse).toList();
    }

    public CursoSetorResponse buscarPorId(Long id) {
        return toResponse(cursoSetorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Curso/Setor não encontrado.")));
    }

    public CursoSetorResponse atualizar(Long id, CursoSetorRequest request) {
        CursoSetor entidade = cursoSetorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Curso/Setor não encontrado."));
        String nome = request.nome().trim();
        if (cursoSetorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um curso/setor com o nome \"" + nome + "\".");
        }
        entidade.setNome(nome);
        if (request.tipo() != null) entidade.setTipo(request.tipo());
        return toResponse(cursoSetorRepository.save(entidade));
    }

    public void inativar(Long id) {
        CursoSetor entidade = cursoSetorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Curso/Setor não encontrado."));
        entidade.setAtivo(false);
        cursoSetorRepository.save(entidade);
    }

    public void reativar(Long id) {
        CursoSetor entidade = cursoSetorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Curso/Setor não encontrado."));
        entidade.setAtivo(true);
        cursoSetorRepository.save(entidade);
    }

    private CursoSetorResponse toResponse(CursoSetor cs) {
        return new CursoSetorResponse(cs.getId(), cs.getNome(), cs.getTipo(), cs.isAtivo(), cs.getDataCadastro());
    }
}
