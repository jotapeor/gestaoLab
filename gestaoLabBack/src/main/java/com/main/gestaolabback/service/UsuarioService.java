package com.main.gestaolabback.service;

import com.main.gestaolabback.dto.MeuPerfilRequest;
import com.main.gestaolabback.dto.ResponsavelResponse;
import com.main.gestaolabback.dto.UsuarioAutenticado;
import com.main.gestaolabback.dto.UsuarioRequest;
import com.main.gestaolabback.dto.UsuarioResponse;
import com.main.gestaolabback.model.CursoSetor;
import com.main.gestaolabback.model.PerfilUsuario;
import com.main.gestaolabback.model.Usuario;
import com.main.gestaolabback.repository.CursoSetorRepository;
import com.main.gestaolabback.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private static final int PAGE_SIZE = 20;

    private final UsuarioRepository usuarioRepository;
    private final CursoSetorRepository cursoSetorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          CursoSetorRepository cursoSetorRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cursoSetorRepository = cursoSetorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UsuarioResponse> listar(UsuarioAutenticado autenticado,
                                        String busca, PerfilUsuario perfil,
                                        Long cursoSetorId, Boolean ativo, int page) {
        String buscaFiltro = (busca != null && !busca.isBlank()) ? busca.trim() : null;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Usuario> resultado;
        if (autenticado.perfil().equals("COORDENADOR")) {
            resultado = usuarioRepository.findWithFilters(buscaFiltro, perfil, cursoSetorId, ativo, pageable);
        } else {
            resultado = usuarioRepository.findByResponsavelWithFilters(
                    autenticado.id(), buscaFiltro, perfil, cursoSetorId, ativo, pageable);
        }
        return resultado.map(this::toResponse);
    }

    public UsuarioResponse buscarPorId(Long id, UsuarioAutenticado autenticado) {
        Usuario usuario = findOrThrow(id);
        if (!autenticado.perfil().equals("COORDENADOR")) {
            if (usuario.getResponsavel() == null || !usuario.getResponsavel().getId().equals(autenticado.id())) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Acesso negado.");
            }
        }
        return toResponse(usuario);
    }

    public UsuarioResponse criar(UsuarioRequest request) {
        if (request.senhaProvisoria() == null || request.senhaProvisoria().isBlank()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Senha provisória é obrigatória no cadastro.");
        }
        if (request.senhaProvisoria().length() < 8) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Senha provisória deve ter no mínimo 8 caracteres.");
        }

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um usuário com o e-mail \"" + email + "\".");
        }

        String matricula = normalizeMatricula(request.matricula());
        if (matricula != null && usuarioRepository.existsByMatricula(matricula)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um usuário com a matrícula \"" + matricula + "\".");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        usuario.setPerfil(request.perfil() != null ? request.perfil() : PerfilUsuario.USUARIO);
        usuario.setSenha(passwordEncoder.encode(request.senhaProvisoria()));
        usuario.setPrimeiroAcesso(true);
        usuario.setAtivo(true);

        aplicarCursoSetor(usuario, request.cursoSetorId());
        aplicarResponsavel(usuario, request.responsavelId(), null);

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = findOrThrow(id);

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um usuário com o e-mail \"" + email + "\".");
        }

        String matricula = normalizeMatricula(request.matricula());
        if (matricula != null && usuarioRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409),
                    "Já existe um usuário com a matrícula \"" + matricula + "\".");
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        if (request.perfil() != null) usuario.setPerfil(request.perfil());

        aplicarCursoSetor(usuario, request.cursoSetorId());
        aplicarResponsavel(usuario, request.responsavelId(), id);

        return toResponse(usuarioRepository.save(usuario));
    }

    public void inativar(Long id, UsuarioAutenticado autenticado) {
        Usuario usuario = findOrThrow(id);
        if (usuario.getId().equals(autenticado.id())) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Você não pode inativar sua própria conta.");
        }
        if (usuario.getPerfil() == PerfilUsuario.COORDENADOR && usuario.isAtivo()) {
            long ativos = usuarioRepository.countByPerfilAndAtivo(PerfilUsuario.COORDENADOR, true);
            if (ativos <= 1) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "O sistema precisa ter pelo menos um coordenador ativo.");
            }
        }
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void reativar(Long id) {
        Usuario usuario = findOrThrow(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    public void redefinirSenha(Long id, String novaSenhaProvisoria) {
        if (novaSenhaProvisoria == null || novaSenhaProvisoria.length() < 8) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400),
                    "Nova senha provisória deve ter no mínimo 8 caracteres.");
        }
        Usuario usuario = findOrThrow(id);
        usuario.setSenha(passwordEncoder.encode(novaSenhaProvisoria));
        usuario.setPrimeiroAcesso(true);
        usuarioRepository.save(usuario);
    }

    public List<ResponsavelResponse> listarResponsaveis() {
        return usuarioRepository
                .findByPerfilInAndAtivoTrueOrderByNome(
                        List.of(PerfilUsuario.PROFESSOR, PerfilUsuario.COORDENADOR))
                .stream()
                .map(u -> new ResponsavelResponse(u.getId(), u.getNome()))
                .toList();
    }

    public UsuarioResponse me(Long userId) {
        return toResponse(findOrThrow(userId));
    }

    public UsuarioResponse atualizarMe(Long userId, MeuPerfilRequest request) {
        Usuario usuario = findOrThrow(userId);
        usuario.setNome(request.nome().trim());
        return toResponse(usuarioRepository.save(usuario));
    }

    private void aplicarCursoSetor(Usuario usuario, Long cursoSetorId) {
        if (cursoSetorId == null) {
            usuario.setCursoSetor(null);
            return;
        }
        CursoSetor cs = cursoSetorRepository.findById(cursoSetorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(422),
                        "Curso/Setor não encontrado."));
        if (!cs.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O curso/setor informado está inativo.");
        }
        usuario.setCursoSetor(cs);
    }

    private void aplicarResponsavel(Usuario usuario, Long responsavelId, Long usuarioId) {
        if (responsavelId == null) {
            usuario.setResponsavel(null);
            return;
        }
        if (usuarioId != null && responsavelId.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O responsável não pode ser o próprio usuário.");
        }
        Usuario responsavel = findOrThrow(responsavelId);
        if (!responsavel.isAtivo()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O responsável informado está inativo.");
        }
        if (responsavel.getPerfil() != PerfilUsuario.PROFESSOR
                && responsavel.getPerfil() != PerfilUsuario.COORDENADOR) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "O responsável deve ter perfil Professor ou Coordenador.");
        }
        usuario.setResponsavel(responsavel);
    }

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404),
                        "Usuário não encontrado."));
    }

    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse.CursoSetorResumo cs = u.getCursoSetor() != null
                ? new UsuarioResponse.CursoSetorResumo(u.getCursoSetor().getId(), u.getCursoSetor().getNome())
                : null;
        UsuarioResponse.UsuarioResumo resp = u.getResponsavel() != null
                ? new UsuarioResponse.UsuarioResumo(u.getResponsavel().getId(), u.getResponsavel().getNome())
                : null;
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getMatricula(), u.getEmail(),
                u.getPerfil(), u.isAtivo(), u.isPrimeiroAcesso(), u.getDataCriacao(),
                cs, resp);
    }

    private String normalizeMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) return null;
        return matricula.trim();
    }
}
