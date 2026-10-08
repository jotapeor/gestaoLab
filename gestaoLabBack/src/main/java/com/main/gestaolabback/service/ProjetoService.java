package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.ParticipanteDisponivelResponse;
import com.main.gestaolabback.dto.ProjetoDetalheResponse;
import com.main.gestaolabback.dto.ProjetoRequest;
import com.main.gestaolabback.dto.ProjetoResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Projeto;
import com.main.gestaolabback.model.TipoProjeto;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.ProjetoRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjetoService {

    private static final int PAGE_SIZE = 20;

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProjetoService(ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Page<ProjetoResponse> listar(UsuarioAutenticado autenticado,
                                        String busca, TipoProjeto tipo,
                                        Long orientadorId, Long participanteId,
                                        Boolean ativo, int page) {
        String buscaFiltro = (busca != null && !busca.isBlank()) ? busca.trim() : null;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Long orientadorFiltro = orientadorId;
        if ("PROFESSOR".equals(autenticado.perfil())) {
            orientadorFiltro = autenticado.id();
        }

        Page<Projeto> resultado = projetoRepository.findWithFilters(
                buscaFiltro, tipo, orientadorFiltro, participanteId, ativo, pageable);

        return resultado.map(p -> toResponse(p, projetoRepository.countParticipantes(p.getId())));
    }

    public List<ProjetoResponse> meusProjetos(UsuarioAutenticado autenticado) {
        if ("USUARIO".equals(autenticado.perfil())) {
            return projetoRepository.findActiveByParticipanteId(autenticado.id())
                    .stream()
                    .map(p -> toResponse(p, projetoRepository.countParticipantes(p.getId())))
                    .toList();
        }
        return projetoRepository.findMeusByOrientadorId(autenticado.id())
                .stream()
                .map(p -> toResponse(p, projetoRepository.countParticipantes(p.getId())))
                .toList();
    }

    public List<ProjetoResponse> projetosAtivosDeUsuario(Long usuarioId) {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        java.util.List<Projeto> result = new java.util.ArrayList<>();
        for (Projeto p : projetoRepository.findMeusByOrientadorId(usuarioId)) {
            if (seen.add(p.getId())) result.add(p);
        }
        for (Projeto p : projetoRepository.findActiveByParticipanteId(usuarioId)) {
            if (seen.add(p.getId())) result.add(p);
        }
        result.sort(java.util.Comparator.comparing(Projeto::getTitulo,
                String.CASE_INSENSITIVE_ORDER));
        return result.stream()
                .map(p -> toResponse(p, projetoRepository.countParticipantes(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjetoDetalheResponse buscarPorId(Long id, UsuarioAutenticado autenticado) {
        Projeto projeto = projetoRepository.findByIdWithParticipantes(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Projeto não encontrado."));

        verificarAcessoLeitura(projeto, autenticado);
        return toDetalheResponse(projeto);
    }

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request, UsuarioAutenticado autenticado) {
        Projeto projeto = new Projeto();
        projeto.setTitulo(request.titulo().trim());
        projeto.setTipo(request.tipo());
        projeto.setDescricao(request.descricao());
        aplicarOrientador(projeto, request, autenticado);
        return toResponse(projetoRepository.save(projeto), 0);
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest request, UsuarioAutenticado autenticado) {
        Projeto projeto = findOrThrow(id);
        verificarGestao(projeto, autenticado);
        projeto.setTitulo(request.titulo().trim());
        projeto.setTipo(request.tipo());
        projeto.setDescricao(request.descricao());
        aplicarOrientador(projeto, request, autenticado);
        return toResponse(projetoRepository.save(projeto), projetoRepository.countParticipantes(id));
    }

    @Transactional
    public void inativar(Long id, UsuarioAutenticado autenticado) {
        Projeto projeto = findOrThrow(id);
        verificarGestao(projeto, autenticado);
        projeto.setAtivo(false);
        projetoRepository.save(projeto);
    }

    @Transactional
    public void reativar(Long id, UsuarioAutenticado autenticado) {
        Projeto projeto = findOrThrow(id);
        verificarGestao(projeto, autenticado);
        projeto.setAtivo(true);
        projetoRepository.save(projeto);
    }

    @Transactional
    public ProjetoDetalheResponse adicionarParticipante(Long projetoId, Long usuarioId,
                                                         UsuarioAutenticado autenticado) {
        Projeto projeto = projetoRepository.findByIdWithParticipantes(projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Projeto não encontrado."));
        verificarGestao(projeto, autenticado);

        if (!projeto.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Não é possível adicionar participantes a um projeto inativo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "O usuário informado está inativo.");
        }
        if (usuario.getPerfil() != PerfilUsuario.USUARIO) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Somente usuários com perfil Usuário podem ser adicionados como participantes.");
        }

        boolean jaParticipa = projeto.getUsuarios().stream().anyMatch(u -> u.getId().equals(usuarioId));
        if (jaParticipa) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "O usuário já é participante deste projeto.");
        }

        usuario.adicionarProjeto(projeto);
        usuarioRepository.save(usuario);

        Projeto atualizado = projetoRepository.findByIdWithParticipantes(projetoId).orElseThrow();
        return toDetalheResponse(atualizado);
    }

    @Transactional
    public ProjetoDetalheResponse removerParticipante(Long projetoId, Long usuarioId,
                                                       UsuarioAutenticado autenticado) {
        Projeto projeto = projetoRepository.findByIdWithParticipantes(projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Projeto não encontrado."));
        verificarGestao(projeto, autenticado);

        if (!projeto.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Não é possível remover participantes de um projeto inativo.");
        }

        boolean participava = projeto.getUsuarios().stream().anyMatch(u -> u.getId().equals(usuarioId));
        if (!participava) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404),
                    "Usuário não é participante deste projeto.");
        }

        Usuario participante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Usuário não encontrado."));
        participante.removerProjeto(projeto);
        usuarioRepository.save(participante);

        Projeto atualizado = projetoRepository.findByIdWithParticipantes(projetoId).orElseThrow();
        return toDetalheResponse(atualizado);
    }

    public List<ParticipanteDisponivelResponse> listarParticipantesDisponiveis(String busca) {
        String buscaFiltro = (busca != null && !busca.isBlank()) ? busca.trim() : null;
        return usuarioRepository.findParticipantesDisponiveis(buscaFiltro, PageRequest.of(0, 20))
                .getContent()
                .stream()
                .map(u -> new ParticipanteDisponivelResponse(
                        u.getId(), u.getNome(), u.getMatricula(),
                        u.getCursoSetor() != null ? u.getCursoSetor().getNome() : null))
                .toList();
    }

    private void aplicarOrientador(Projeto projeto, ProjetoRequest request, UsuarioAutenticado autenticado) {
        Long orientadorId;
        if ("PROFESSOR".equals(autenticado.perfil())) {
            orientadorId = autenticado.id();
        } else {
            orientadorId = request.orientadorId();
        }
        if (orientadorId == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "Orientador é obrigatório.");
        }
        Usuario orientador = usuarioRepository.findById(orientadorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Orientador não encontrado."));
        if (!orientador.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422), "O orientador informado está inativo.");
        }
        if (orientador.getPerfil() != PerfilUsuario.PROFESSOR && orientador.getPerfil() != PerfilUsuario.COORDENADOR) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O orientador deve ter perfil Professor ou Coordenador.");
        }
        projeto.setOrientador(orientador);
    }

    private void verificarGestao(Projeto projeto, UsuarioAutenticado autenticado) {
        if ("COORDENADOR".equals(autenticado.perfil())) return;
        if ("PROFESSOR".equals(autenticado.perfil())) {
            if (projeto.getOrientador() != null && projeto.getOrientador().getId().equals(autenticado.id())) return;
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
    }

    private void verificarAcessoLeitura(Projeto projeto, UsuarioAutenticado autenticado) {
        if ("COORDENADOR".equals(autenticado.perfil())) return;
        if ("PROFESSOR".equals(autenticado.perfil())) {
            if (projeto.getOrientador() != null && projeto.getOrientador().getId().equals(autenticado.id())) return;
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        }
        boolean participa = projeto.getUsuarios().stream().anyMatch(u -> u.getId().equals(autenticado.id()));
        if (!participa) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
        }
    }

    private Projeto findOrThrow(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Projeto não encontrado."));
    }

    private ProjetoResponse toResponse(Projeto p, long qtdParticipantes) {
        ProjetoResponse.OrientadorResumo orientador = p.getOrientador() != null
                ? new ProjetoResponse.OrientadorResumo(p.getOrientador().getId(), p.getOrientador().getNome())
                : null;
        return new ProjetoResponse(p.getId(), p.getTitulo(), p.getTipo(), orientador,
                p.getDescricao(), p.isAtivo(), p.getDataCadastro(), (int) qtdParticipantes);
    }

    private ProjetoDetalheResponse toDetalheResponse(Projeto p) {
        ProjetoDetalheResponse.OrientadorResumo orientador = p.getOrientador() != null
                ? new ProjetoDetalheResponse.OrientadorResumo(p.getOrientador().getId(), p.getOrientador().getNome())
                : null;
        List<ProjetoDetalheResponse.ParticipanteResumo> participantes = p.getUsuarios().stream()
                .map(u -> new ProjetoDetalheResponse.ParticipanteResumo(
                        u.getId(), u.getNome(), u.getMatricula(),
                        u.getCursoSetor() != null ? u.getCursoSetor().getNome() : null,
                        u.isAtivo()))
                .toList();
        return new ProjetoDetalheResponse(p.getId(), p.getTitulo(), p.getTipo(), orientador,
                p.getDescricao(), p.isAtivo(), p.getDataCadastro(), participantes);
    }
}
